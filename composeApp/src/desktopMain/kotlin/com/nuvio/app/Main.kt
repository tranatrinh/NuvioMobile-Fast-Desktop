package com.nuvio.app

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toPainter
import javax.imageio.ImageIO
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.RECT
import com.sun.jna.win32.StdCallLibrary
import java.awt.GraphicsEnvironment
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.AWTEvent
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

private interface WinUser32 : StdCallLibrary {
    fun GetWindowLongPtrA(hWnd: HWND, nIndex: Int): Long
    fun SetWindowLongPtrA(hWnd: HWND, nIndex: Int, dwNewLong: Long): Long
    fun GetWindowRect(hWnd: HWND, lpRect: RECT): Boolean
    fun SetWindowPos(
        hWnd: HWND,
        hWndInsertAfter: HWND?,
        X: Int,
        Y: Int,
        cx: Int,
        cy: Int,
        uFlags: Int
    ): Boolean

    companion object {
        val INSTANCE: WinUser32 = Native.load("user32", WinUser32::class.java)
    }
}

internal object DesktopFullscreenManager {
    val isFullscreen = mutableStateOf(false)
    var toggleCallback: (() -> Unit)? = null
    var exitCallback: (() -> Unit)? = null

    fun toggle() = toggleCallback?.invoke()
    fun exit() = exitCallback?.invoke()
}

fun main() {
    System.setProperty("jna.library.path", "D:/Tools/nuvio-deps/mpv")
    application {
        val windowState = rememberWindowState(width = 1280.dp, height = 760.dp)
        var savedStyle = 0L
        var savedRect: RECT? = null

        fun setWindowsBorderlessFullscreen(awtWindow: ComposeWindow, enable: Boolean) {
            val hwnd = HWND(Native.getComponentPointer(awtWindow))
            val u32 = WinUser32.INSTANCE
            val GWL_STYLE = -16
            val WS_OVERLAPPEDWINDOW = 0x00CF0000L
            val SWP_FRAMECHANGED = 0x0020
            val SWP_NOOWNERZORDER = 0x0200
            val HWND_TOP = HWND(Pointer.createConstant(0))

            if (enable) {
                savedStyle = u32.GetWindowLongPtrA(hwnd, GWL_STYLE)
                val rect = RECT()
                u32.GetWindowRect(hwnd, rect)
                savedRect = rect

                val gc = awtWindow.graphicsConfiguration
                    ?: GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
                val bounds = gc.bounds
                val scaleX = gc.defaultTransform.scaleX
                val scaleY = gc.defaultTransform.scaleY

                u32.SetWindowLongPtrA(hwnd, GWL_STYLE, savedStyle and WS_OVERLAPPEDWINDOW.inv())
                u32.SetWindowPos(
                    hwnd,
                    HWND_TOP,
                    (bounds.x * scaleX).toInt(),
                    (bounds.y * scaleY).toInt(),
                    (bounds.width * scaleX).toInt(),
                    (bounds.height * scaleY).toInt(),
                    SWP_FRAMECHANGED or SWP_NOOWNERZORDER
                )
                DesktopFullscreenManager.isFullscreen.value = true
            } else {
                if (savedStyle != 0L) {
                    u32.SetWindowLongPtrA(hwnd, GWL_STYLE, savedStyle)
                }
                val r = savedRect
                if (r != null) {
                    u32.SetWindowPos(
                        hwnd,
                        HWND_TOP,
                        r.left,
                        r.top,
                        r.right - r.left,
                        r.bottom - r.top,
                        SWP_FRAMECHANGED or SWP_NOOWNERZORDER
                    )
                }
                DesktopFullscreenManager.isFullscreen.value = false
            }
        }

        val appIconImage = remember {
            Thread.currentThread().contextClassLoader
                .getResourceAsStream("icons/nuvio-app-icon-transparent.png")
                ?.use { ImageIO.read(it) }
        }
        Window(
            onCloseRequest = ::exitApplication,
            title = "Nuvio",
            state = windowState,
            icon = appIconImage?.toPainter()
        ) {
            LaunchedEffect(window, appIconImage) {
                if (appIconImage != null) {
                    window.iconImages = listOf(appIconImage)
                }
            }
            fun toggleFs() {
                setWindowsBorderlessFullscreen(window, !DesktopFullscreenManager.isFullscreen.value)
            }

            fun exitFs() {
                if (DesktopFullscreenManager.isFullscreen.value) {
                    setWindowsBorderlessFullscreen(window, false)
                }
            }

            DesktopFullscreenManager.toggleCallback = ::toggleFs
            DesktopFullscreenManager.exitCallback = ::exitFs

            DisposableEffect(window) {
                val dispatcher = KeyEventDispatcher { event ->
                    if (event.id == KeyEvent.KEY_PRESSED) {
                        val isF11 = event.keyCode == KeyEvent.VK_F11
                        val isAltEnter = event.keyCode == KeyEvent.VK_ENTER && event.isAltDown
                        if (isF11 || isAltEnter) {
                            toggleFs()
                            return@KeyEventDispatcher true
                        }
                        if (event.keyCode == KeyEvent.VK_ESCAPE && DesktopFullscreenManager.isFullscreen.value) {
                            exitFs()
                            return@KeyEventDispatcher true
                        }
                    }
                    false
                }
                val mouseListener = AWTEventListener { event ->
                    if (event is MouseEvent &&
                        event.id == MouseEvent.MOUSE_CLICKED &&
                        event.button == MouseEvent.BUTTON1 &&
                        event.clickCount == 2 &&
                        com.nuvio.app.features.player.DesktopPlayerUiBridge.onToggleFullscreen != null
                    ) {
                        val comp = event.component
                        val h = comp?.height ?: 0
                        // Chi bat double-click o vung video (bo qua thanh dieu khien tren/duoi)
                        if (h > 0 && event.y in 72..(h - 110)) {
                            toggleFs()
                        }
                    }
                }
                KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
                Toolkit.getDefaultToolkit().addAWTEventListener(mouseListener, AWTEvent.MOUSE_EVENT_MASK)
                onDispose {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
                    Toolkit.getDefaultToolkit().removeAWTEventListener(mouseListener)
                }
            }

            App()
        }
    }
}


