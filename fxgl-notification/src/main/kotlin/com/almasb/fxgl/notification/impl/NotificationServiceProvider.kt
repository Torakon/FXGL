/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.notification.impl

import com.almasb.fxgl.core.serialization.Bundle
import com.almasb.fxgl.notification.Notification
import com.almasb.fxgl.notification.NotificationService
import com.almasb.fxgl.time.Timer
import javafx.scene.Group
import javafx.scene.paint.Color
import javafx.util.Duration
import java.util.*

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class NotificationServiceProvider : NotificationService {

    private val ANIMATION_DURATION = Duration.seconds(1.0)
    private val NOTIFICATION_DURATION = Duration.seconds(3.0)

    override var backgroundColor: Color = Color.BLACK
    override var textColor: Color = Color.WHITE

    private val queue = ArrayDeque<Notification>()

    private var showing = false

    private lateinit var root: Group
    private lateinit var timer: Timer

    private val notificationView by lazy { XboxNotificationView() }

    /**
     * Shows a notification with given text.
     * Only 1 notification can be shown at a time.
     * If a notification is being shown already, next notifications
     * will be queued to be shown as soon as space available.
     *
     * @param message the text to show
     */
    override fun pushNotification(message: String) {
        val notification = Notification(message)

        if (showing) {
            queue.add(notification)
        } else {
            showFirstNotification()
            queue.add(notification)
        }
    }

    override fun provideOverlayRoot(overlayRoot: Group) {
        root = overlayRoot
    }

    override fun provideTimer(timer: Timer) {
        this.timer = timer
    }

    private fun nextNotification() {
        if (queue.isNotEmpty()) {
            val n = queue.poll()
            notificationView.push(n)

            fireAndScheduleNextNotification(n)
        } else {
            notificationView.playOutAnimation()

            timer.runOnceAfter(this::checkLastPop, ANIMATION_DURATION)
        }
    }

    private fun checkLastPop() {
        if (queue.isEmpty()) {
            root.children -= notificationView
            showing = false
        } else {
            // play in animation
            notificationView.playInAnimation()

            timer.runOnceAfter(this::nextNotification, ANIMATION_DURATION)
        }
    }

    private fun showFirstNotification() {
        showing = true
        root.children += notificationView

        // play in animation
        notificationView.playInAnimation()

        timer.runOnceAfter(this::nextNotification, ANIMATION_DURATION)
    }

    private fun fireAndScheduleNextNotification(notification: Notification) {
        // schedule next
        timer.runOnceAfter(this::nextNotification, NOTIFICATION_DURATION)
    }

    override fun onMainLoopStarting() {
    }

    override fun onExit() {
    }

    override fun onUpdate(tpf: Double) {
    }

    override fun write(bundle: Bundle) {
    }

    override fun read(bundle: Bundle) {
    }
}