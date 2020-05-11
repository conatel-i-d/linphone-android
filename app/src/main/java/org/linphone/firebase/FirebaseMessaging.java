/*
 * Copyright (c) 2010-2019 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.firebase;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.linphone.LinphoneContext;
import org.linphone.LinphoneManager;
import org.linphone.core.Core;
import org.linphone.core.tools.Log;
import org.linphone.settings.LinphonePreferences;
import org.linphone.utils.LinphoneUtils;

public class FirebaseMessaging extends FirebaseMessagingService {
    private Runnable mPushReceivedRunnable =
        new Runnable() {
            @Override
            public void run() {
                if (NotificationsUtils.isAppIsInBackground(getApplicationContext()) == true) {
                    // app is in foreground, broadcast the push message
                    android.util.Log.i("FirebaseMessaging", "App is in Background");
                } else {
                    // If the app is in background, firebase itself handles the notification
                    android.util.Log.i("FirebaseMessaging", "App is NOT in Background");
                }
            }
        };

    private Runnable mPushReceivedRunnableBak =
            new Runnable() {
                @Override
                public void run() {
                    android.util.Log.i(
                            "FirebaseMessaging", "[Push Notification] [mPushReceivedRunnable] run");
                    if (!LinphoneContext.isReady()) {
                        android.util.Log.i(
                                "FirebaseMessaging",
                                "[Push Notification] [mPushReceivedRunnable] LinphoneContext.isReady() == False");
                        android.util.Log.i(
                                "FirebaseMessaging", "[Push Notification] Starting context");
                        new LinphoneContext(getApplicationContext());
                        LinphoneContext.instance().start(true); // Verificar!!!
                    } else {
                        android.util.Log.i(
                                "FirebaseMessaging",
                                "[Push Notification] [mPushReceivedRunnable] LinphoneContext.isReady() == True");
                        Log.i("[Push Notification] Notifying Core");
                        if (LinphoneManager.getInstance() != null) {
                            android.util.Log.i(
                                    "FirebaseMessaging",
                                    "[Push Notification] [mPushReceivedRunnable] LinphoneManager.getInstance() != null");
                            Core core = LinphoneManager.getCore();
                            if (core != null) {
                                core.ensureRegistered();
                                android.util.Log.i(
                                        "FirebaseMessaging",
                                        "[Push Notification] [mPushReceivedRunnable] core != null");
                            }
                        }
                    }
                }
            };

    public FirebaseMessaging() {}

    @Override
    public void onNewToken(final String token) {
        android.util.Log.i("FirebaseIdService", "[Push Notification] Refreshed token: " + token);

        LinphoneUtils.dispatchOnUIThread(
                new Runnable() {
                    @Override
                    public void run() {
                        LinphonePreferences.instance().setPushNotificationRegistrationID(token);
                    }
                });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        android.util.Log.i("FirebaseMessaging", "[Push Notification] Received");

        android.util.Log.i("FirebaseMessaging", "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            android.util.Log.i(
                    "FirebaseMessaging",
                    "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            android.util.Log.i(
                    "FirebaseMessaging", "Data Payload: " + remoteMessage.getData().toString());
        }

        if (NotificationsUtils.isAppIsInBackground(getApplicationContext()) == true) {
            // app is in foreground, broadcast the push message
            android.util.Log.i("FirebaseMessaging", "App is in Background");
        } else {
            // If the app is in background, firebase itself handles the notification
            android.util.Log.i("FirebaseMessaging", "App is NOT in Background");
        }

        LinphoneUtils.dispatchOnUIThread(mPushReceivedRunnable);
    }

    private void handleNotification(String message) {
        android.util.Log.i(
                "FirebaseMessaging",
                "" + NotificationsUtils.isAppIsInBackground(getApplicationContext()));

        if (!NotificationsUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            android.util.Log.i("FirebaseMessaging", "Notification message: " + message);
        } else {
            // If the app is in background, firebase itself handles the notification
            android.util.Log.i(
                    "FirebaseMessaging",
                    "!NotificationsUtils.isAppIsInBackground(getApplicationContext()) == False");
        }
    }
}
