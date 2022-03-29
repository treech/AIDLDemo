// INotificationInterface.aidl
package com.treech.notificationdemo;

// Declare any non-default types here with import statements

interface INotificationInterface {
    void addNotify();
    void consumeNotify(int notifyId);
}