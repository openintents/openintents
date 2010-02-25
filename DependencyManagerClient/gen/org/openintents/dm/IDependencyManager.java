/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /media/EEE42F21E42EEC11/Users/Admin/workspace/DependencyManagerClient/src/org/openintents/dm/IDependencyManager.aidl
 */
package org.openintents.dm;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
import java.util.List;
/**
 * Service interface for DependencyManager.
 */
public interface IDependencyManager extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.openintents.dm.IDependencyManager
{
private static final java.lang.String DESCRIPTOR = "org.openintents.dm.IDependencyManager";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an IDependencyManager interface,
 * generating a proxy if needed.
 */
public static org.openintents.dm.IDependencyManager asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.openintents.dm.IDependencyManager))) {
return ((org.openintents.dm.IDependencyManager)iin);
}
return new org.openintents.dm.IDependencyManager.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_resolveDependencies:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.resolveDependencies(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_scanPackageForDependencies:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.List<android.content.Intent> _result = this.scanPackageForDependencies(_arg0);
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_removeResolvableIntents:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<android.content.Intent> _arg0;
_arg0 = data.createTypedArrayList(android.content.Intent.CREATOR);
java.util.List<android.content.Intent> _result = this.removeResolvableIntents(_arg0);
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_displayChoicesForIntents:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<android.content.Intent> _arg0;
_arg0 = data.createTypedArrayList(android.content.Intent.CREATOR);
this.displayChoicesForIntents(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.openintents.dm.IDependencyManager
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
   * Resolve dependencies for the given package name. That encompasses:
   * - Scanning the package for dependency information
   * - Querying the system for unmet dependencies
   * - Querying data sources for packages that would meet those dependencies
   * - Displaying the results, and offering them to the user for installation.
   */
public void resolveDependencies(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_resolveDependencies, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
   * Returns the list of Intents specified as mandatory dependencies in the
   * named package, or null if no such information was found.
   */
public java.util.List<android.content.Intent> scanPackageForDependencies(java.lang.String packageName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<android.content.Intent> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(packageName);
mRemote.transact(Stub.TRANSACTION_scanPackageForDependencies, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(android.content.Intent.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
   * Accepts a list of Intents, and filters out those that can currently be
   * served by the system. It's just a thin wrapper around PackageManager, but
   * honours the de.finkhaeuser.dm.extras.COMPONENT_TYPE extra as set by
   * scanPackageForDependencies, if present. Returns the remaining Intents that
   * cannot be served by the system at the moment.
   */
public java.util.List<android.content.Intent> removeResolvableIntents(java.util.List<android.content.Intent> intents) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<android.content.Intent> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(intents);
mRemote.transact(Stub.TRANSACTION_removeResolvableIntents, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(android.content.Intent.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
   * Displays a dialog with a choice of packages that would serve the specified
   * intents.
   */
public void displayChoicesForIntents(java.util.List<android.content.Intent> intents) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(intents);
mRemote.transact(Stub.TRANSACTION_displayChoicesForIntents, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_resolveDependencies = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_scanPackageForDependencies = (IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_removeResolvableIntents = (IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_displayChoicesForIntents = (IBinder.FIRST_CALL_TRANSACTION + 3);
}
/**
   * Resolve dependencies for the given package name. That encompasses:
   * - Scanning the package for dependency information
   * - Querying the system for unmet dependencies
   * - Querying data sources for packages that would meet those dependencies
   * - Displaying the results, and offering them to the user for installation.
   */
public void resolveDependencies(java.lang.String packageName) throws android.os.RemoteException;
/**
   * Returns the list of Intents specified as mandatory dependencies in the
   * named package, or null if no such information was found.
   */
public java.util.List<android.content.Intent> scanPackageForDependencies(java.lang.String packageName) throws android.os.RemoteException;
/**
   * Accepts a list of Intents, and filters out those that can currently be
   * served by the system. It's just a thin wrapper around PackageManager, but
   * honours the de.finkhaeuser.dm.extras.COMPONENT_TYPE extra as set by
   * scanPackageForDependencies, if present. Returns the remaining Intents that
   * cannot be served by the system at the moment.
   */
public java.util.List<android.content.Intent> removeResolvableIntents(java.util.List<android.content.Intent> intents) throws android.os.RemoteException;
/**
   * Displays a dialog with a choice of packages that would serve the specified
   * intents.
   */
public void displayChoicesForIntents(java.util.List<android.content.Intent> intents) throws android.os.RemoteException;
}
