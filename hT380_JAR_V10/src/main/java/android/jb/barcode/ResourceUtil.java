package android.jb.barcode;

import android.content.Context;
import android.util.Log;

public class ResourceUtil {

	public static int getLayoutResIDByName(Context context, String name) {
		return context.getResources().getIdentifier(name, "layout",
				context.getPackageName());
	}

	public static int getIdResIDByName(Context context, String name) {
		return context.getResources().getIdentifier(name, "id",
				context.getPackageName());
	}

	public static int getStringResIDByName(Context context, String name) {
		Log.v("jiebao","getStringResIDByName context: "+context);
		return context.getResources().getIdentifier(name, "string",
				context.getPackageName());
	}

	public static int getDrawableResIDByName(Context context, String name) {
		return context.getResources().getIdentifier(name, "drawable",
				context.getPackageName());
	}

	public static int getRawResIDByName(Context context, String name) {
		return context.getResources().getIdentifier(name, "raw",
				context.getPackageName());
	}
}
