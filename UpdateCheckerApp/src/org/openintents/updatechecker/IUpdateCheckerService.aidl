package org.openintents.updatechecker;

import org.openintents.updatechecker.IUpdateCheckerServiceCallback;

/**
 * Example of defining an interface for calling on to a remote service
 * (running in another process).
 */
interface IUpdateCheckerService {
    
    void checkForUpdate(String link, IUpdateCheckerServiceCallback cb);
    
}