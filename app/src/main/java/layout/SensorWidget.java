package layout;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import lab.android.bartosz.ssms.MainActivity;
import lab.android.bartosz.ssms.R;
import lab.android.bartosz.ssms.SensorData;
import lab.android.bartosz.ssms.SensorDataDbHelper;

/**
 * Implementation of App Widget functionality.
 */
public class SensorWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        SensorDataDbHelper sensorDataDbHelper = new SensorDataDbHelper(context);
        SensorData last = sensorDataDbHelper.getLast();

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sensor_widget);
        if(last.getId()!=-1) {
            views.setTextViewText(R.id.widgetDeviceIDTextView, String.valueOf(last.getSensorId()));
            views.setTextViewText(R.id.widgetHumidityTextView, String.valueOf(last.getHumidity()));
            views.setTextViewText(R.id.widgetTemperatureTextView, String.valueOf(last.getTemperature()));
            views.setTextViewText(R.id.widgetDateTextView, new StringBuilder(last.getDate().getHours() + ":" + last.getDate().getMinutes() + ":" + last.getDate().getSeconds()).toString());
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
        views.setOnClickPendingIntent(R.id.widgetLayout,pendingIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

