package com.mortenjust.notificationmaker;

//import android.app.Notification;
//import android.app.NotificationManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import java.util.Random;
import java.util.Set;


/**
 * Created by mortenjust on 3/9/16.
 */
public class NotificationAssembler {

    SharedPreferences prefs;
    NotificationCompat.Builder builder;
    NotificationCompat.WearableExtender wearableExtender;

    NotificationCompat notification;
    Context context;

    public NotificationAssembler(Context context){
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Ints are saved as Strings
    Integer getPrefInt(String key) {
        String s = prefs.getString(key, "0");
        Integer i = Integer.parseInt(s);
        return i;
    }

    String getPrefString(String key){
        return prefs.getString(key, "");
    }
    Boolean getPrefBool(String key) {
        return prefs.getBoolean(key, false);
    }

    void postNotification(){
        // indirect values

        Log.d("mj.", "ready to send notification");


        //noinspection ResourceType

        builder  = new NotificationCompat.Builder(context)
                        .setContentTitle(getPrefString("content_title"))
                        .setContentText(getPrefString("content_text"))
                        .setContentInfo(getPrefString("content_info"))
                        .setCategory(getPrefString("category"))
                        .setGroup(getPrefString("group"))
                        .setGroupSummary(getPrefBool("group_summary"))
                        .setPriority(getPrefInt("priority"))
                        .setVisibility(getPrefInt("visibility"))
                        .setOnlyAlertOnce(getPrefBool("only_alert_once"))
                        .setOngoing(getPrefBool("ongoing"))
                        .setUsesChronometer(getPrefBool("uses_chronometer"))
                        .setSmallIcon(R.drawable.ic_small_icon)
                        .addPerson(getPrefString("person"))
                        .setAutoCancel(getPrefBool("auto_cancel"))
                ;

        addActionsFromPref();
        setVibrationFromPref();
        setLargeIconFromPref();

        // wearable extender
        if(getPrefBool("enable_wearable_extender")){
            wearableExtender = new NotificationCompat.WearableExtender();
            addWearableActionsFromPref();
            setLongScreenTimeout();
            setScrolledToBottom();
            setWearableBackground();
            addWearablePages();
            builder.extend(wearableExtender);
        }

        setStyleFromPref(); // must be last because mediastyle wants to override stuff
        Integer notificationId = getNotificationId();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    @NonNull
    private Integer getNotificationId() {
        Integer notificationId;
        Boolean updateNotification = getPrefBool("update_notification");
        if(updateNotification){
            notificationId = 1337;
        } else {
            Random r = new Random();
            notificationId = r.nextInt(63000 - 100) + 63000;
        }
        return notificationId;
    }

    private void setLongScreenTimeout(){
        if(getPrefBool("long_screen_timeout")){
            wearableExtender.setHintScreenTimeout(NotificationCompat.WearableExtender.SCREEN_TIMEOUT_LONG);
        }
    }

    private void setScrolledToBottom(){
        if(getPrefBool("start_scrolled_to_bottom")){
            wearableExtender.setStartScrollBottom(true);
        }
    }

    private void setRemoteInput(){
        CharSequence[] options = {"Yes", "No", "Call me maybe"};

        if(getPrefBool("use_remote_input")){
            RemoteInput remoteInput = new RemoteInput.Builder("key_text_reply")
                    .setLabel("Reply now")
                    .setChoices(options)
                    .build();

            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(R.drawable.ic_reply_white_36dp,
                            "Reply", null)
                            .addRemoteInput(remoteInput)
                            .build();

            builder.addAction(action);
        }
    }

    private void setWearableBackground(){
        if(getPrefBool("background_image")){
            wearableExtender.setBackground(getBitmapFromResource(R.drawable.album_cover));
        }
    }

    private void addWearablePages(){
        if(getPrefBool("add_wearable_pages")){

            NotificationCompat.BigTextStyle textStyle = new NotificationCompat.BigTextStyle();
            textStyle.bigText("\"It was the BigTextStyle on Page 1 of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness, it was the epoch of belief, it was the epoch of incredulity, it was the season of Light, it was the season of Darkness, it was the spring of hope, it was the winter of despair, we had everything before us, we had nothing before us, we were all going direct to Heaven, we were all going direct the other way – in short, the period was so far like the present period, that some of its noisiest authorities insisted on its being received, for good or for evil, in the superlative degree of comparison only.");


            Notification page1;
            page1 = new NotificationCompat.Builder(context)
                    .setStyle(textStyle)
                    .build();

            Notification page2 = new Notification.Builder(context)
                .setStyle(new Notification.BigTextStyle().bigText("\"It was the BigTextStyle on Page 2 of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness, it was the epoch of belief, it was the epoch of incredulity, it was the season of Light, it was the season of Darkness, it was the spring of hope, it was the winter of despair, we had everything before us, we had nothing before us, we were all going direct to Heaven, we were all going direct the other way – in short, the period was so far like the present period, that some of its noisiest authorities insisted on its being received, for good or for evil, in the superlative degree of comparison only.\";"))
                .build();

            wearableExtender.addPage(page1);
            wearableExtender.addPage(page2);
         }
    }





    private void setStyleFromPref() {
        switch(getPrefString("use_style")){
            case "use_big_picture_style":
                Bitmap bigPicture = getBitmapFromResource(R.drawable.big_picture_dog);
                builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigPicture));
                break;
            case "use_big_text_style":
                String bigText = "It was the BigTextStyle of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness, it was the epoch of belief, it was the epoch of incredulity, it was the season of Light, it was the season of Darkness, it was the spring of hope, it was the winter of despair, we had everything before us, we had nothing before us, we were all going direct to Heaven, we were all going direct the other way – in short, the period was so far like the present period, that some of its noisiest authorities insisted on its being received, for good or for evil, in the superlative degree of comparison only.";
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
                break;
            case "use_inbox_style":
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                        .addLine("Line one is here")
                        .addLine("This is line two")
                        .addLine("And three")
                        .addLine("And Four")
                        .setBigContentTitle("Content title here")
                        .setSummaryText("Wait, there's 35 more");
                builder.setStyle(inboxStyle);
                break;
            case "use_media_style":
//                Notification.MediaStyle mediaStyle = new Notification.MediaStyle();
//
//                /// http://developer.android.com/reference/android/app/Notification.MediaStyle.html
//                // should set smallicon to play, largeicon to album art, contenttitle as track title, contentt text as album/artist
//                builder.setLargeIcon(getBitmapFromResource(R.drawable.album_cover));
//                builder.setStyle(a.);

                break;
        }
    }

    private void setLargeIconFromPref() {
        String largeIcon = getPrefString("large_icon");
        switch(largeIcon){
            case "none":
                break;
            case "photo_female_watches_as_eyes":
                builder.setLargeIcon(getBitmapFromResource(R.drawable.photo_female_watches_as_eyes));
                break;
            case "profile_male_finger_pattern":
                builder.setLargeIcon(getBitmapFromResource(R.drawable.profile_male_finger_pattern));
                break;
            case "profile_man_asian":
                builder.setLargeIcon(getBitmapFromResource(R.drawable.profile_man_asian));
                break;
            case "profile_man_hat":
                builder.setLargeIcon(getBitmapFromResource(R.drawable.profile_man_hat));
                break;
            case "profile_woman_hat":
                builder.setLargeIcon(getBitmapFromResource(R.drawable.profile_woman_hat));
                break;
            case "profile_man_watch":
                builder.setLargeIcon(getBitmapFromResource(R.drawable.profile_man_watch));
                break;
            case "launcher_icon":
                builder.setLargeIcon(getBitmapFromResource(R.drawable.ic_launcher));
                break;
        }
    }

    private void setVibrationFromPref() {
        if (getPrefBool("vibrate")){
            builder.setVibrate(new long[]{1000, 1000});
        }
    }

    private void addActionsFromPref() {
        Log.d("mj.actions", "ready to run through");
        Set<String> selectedActions = prefs.getStringSet("actions", null);

        if(selectedActions == null){
            return;
        }


        for(String s : selectedActions){
            Log.d("mj.", "mj.action"+s);
            switch(s){
                case "reply":
                    builder.addAction(createAction(R.drawable.ic_reply_white_36dp, "Reply"));
                    break;
                case "archive":
                    builder.addAction(createAction(R.drawable.ic_archive_white_36dp, "Archive"));
                    break;
                case "comment":
                    builder.addAction(createAction(R.drawable.ic_comment_white_36dp, "Comment"));
                    break;
                case "like":
                    builder.addAction(createAction(R.drawable.ic_thumb_up_white_36dp, "Like"));
                    break;
                case "open":
                    builder.addAction(createAction(R.drawable.ic_album_white_36dp, "Open"));
                    break;
                case "done":
                    builder.addAction(createAction(R.drawable.ic_check_circle_black_24dp, "Done"));
                    break;
            }
        }
    }
    private void addWearableActionsFromPref() {
        Set<String> selectedActions = prefs.getStringSet("wearable_actions", null);

        if(selectedActions == null){
            return;
        }


        for(String s : selectedActions){
            Log.d("mj.", "mj.action"+s);
            switch(s){
                case "reply":
                    wearableExtender.addAction(createAction(R.drawable.ic_reply_white_36dp, "Reply"));
                    break;
                case "archive":
                    wearableExtender.addAction(createAction(R.drawable.ic_archive_white_36dp, "Archive"));
                    break;
                case "comment":
                    wearableExtender.addAction(createAction(R.drawable.ic_comment_white_36dp, "Comment"));
                    break;
                case "like":
                    wearableExtender.addAction(createAction(R.drawable.ic_thumb_up_white_36dp, "Like"));
                    break;
                case "open":
                    wearableExtender.addAction(createAction(R.drawable.ic_album_white_36dp, "Open"));
                    break;
                case "done":
                    wearableExtender.addAction(createAction(R.drawable.ic_check_circle_black_24dp, "Done"));
                    break;
            }
        }
    }

    NotificationCompat.Action createAction(int iconId, String label){
        NotificationCompat.Action action;
        action = new NotificationCompat.Action.Builder(iconId, label, null).build();
        return action;
    }

    Bitmap getBitmapFromResource(int resourceId){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        return bitmap;
    }


}
