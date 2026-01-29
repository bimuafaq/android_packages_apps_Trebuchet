package com.android.quickstep.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.launcher3.Utilities;
import com.android.systemui.shared.recents.model.Task;

import java.util.HashSet;
import java.util.Set;

public class TaskLockState {

    private static final String PREF_LOCKED_TASKS = "pref_locked_tasks";
    private static TaskLockState sInstance;

    private final SharedPreferences mPrefs;
    private final Set<String> mLockedPackages;

    private TaskLockState(Context context) {
        mPrefs = Utilities.getPrefs(context);
        mLockedPackages = new HashSet<>(mPrefs.getStringSet(PREF_LOCKED_TASKS, new HashSet<>()));
    }

    public static TaskLockState getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TaskLockState(context.getApplicationContext());
        }
        return sInstance;
    }

    public boolean isTaskLocked(Task task) {
        return task != null && task.key != null && mLockedPackages.contains(task.key.getPackageName());
    }

    public void setTaskLocked(Task task, boolean locked) {
        if (task == null || task.key == null) return;
        String packageName = task.key.getPackageName();
        if (locked) {
            mLockedPackages.add(packageName);
        } else {
            mLockedPackages.remove(packageName);
        }
        mPrefs.edit().putStringSet(PREF_LOCKED_TASKS, mLockedPackages).apply();
    }
}
