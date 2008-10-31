package org.openintents.updatechecker;

/**
 * Example of a callback interface used by IRemoteService to send
 * synchronous notifications back to its clients.  Note that this is a
 * one-way interface so the server does not block waiting for the client.
 */
oneway interface IUpdateCheckerServiceCallback {
    /**
     * Called when the version has been checked.
     */
    void onVersionChecked(int latestVersion, String newApplicationId, String comment, String latestVersionName);
}