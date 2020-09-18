package com.omys.stickerapp.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.omys.stickerapp.R;
import com.omys.stickerapp.app.AllStickersListActivity;
import com.omys.stickerapp.utils.FunctionsKt;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by SandeepParish on 21/09/18.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "firebase token";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            String clickAction = remoteMessage.getData().get("click_url");

            String imageUri = (remoteMessage.getNotification().getImageUrl() != null) ? remoteMessage.getNotification().getImageUrl().toString() : "";

            showNotification(title, body, imageUri, clickAction);
        }

    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }

    private void showNotification(String notificationTitle, String notificationMessage, String imageUrl, String clickAction) {
        Bitmap notificationBanner = null;
        Intent intent = null;
        if (imageUrl != null && imageUrl.trim().length() > 0) {
            notificationBanner = getBitmapFromUrl(imageUrl);
        }

        if (!FunctionsKt.isAppIsInBackground(this) && clickAction != null && clickAction.length() > 0) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickAction));
        } else {
            intent = new Intent(this, AllStickersListActivity.class);
        }

        PendingIntent pendingIntent = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        String channelName = getString(R.string.default_notification_channel_name);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0});

        if (imageUrl != null && imageUrl.trim().length() > 0 && notificationBanner != null) {
            builder.setLargeIcon(notificationBanner);
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(notificationBanner));
        }

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());

        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableLights(true);
                channel.setVibrationPattern(new long[]{0});
                channel.enableVibration(true);
                manager.createNotificationChannel(channel);
            }

            Notification notification = builder.build();
            manager.notify(1, notification);
        } else {

        }
    }


    /*
     *To get a Bitmap image from the URL received
     * */
    public Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

}
