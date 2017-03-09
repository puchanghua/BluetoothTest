1. 启动蓝牙功能
BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
其次，调用isEnabled()来查询当前蓝牙设备的状态，如果返回为false，则表示蓝牙设备没有开启，接下来你需要封装一个ACTION_REQUEST_ENABLE请求到intent里面，调用startActivityForResult()方法使能蓝牙设备，例如：
if (!mBluetoothAdapter.isEnabled()) {
  Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);   
  startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
 }

2. 查找设备

3. 查询匹配好的设备
在建立连接之前你必须先查询配对好了的蓝牙设备集以便选取一个设备进行通信，例如你可以你可以查询所有配对的蓝牙设备，并使用一个数组适配器将其打印显示出来：
Set<BluetoothDevice> pairedDevices =mBluetoothAdapter.getBondedDevices();
if (pairedDevices.size() > 0) { 
  for (BluetoothDevice device : pairedDevices) { 
    mArrayAdapter.add(device.getName() + "/n" + device.getAddress());    
  }
 }

4. 扫描设备
扫描设备，只需要调用startDiscovery()方法，这个扫描的过程大概持续是12秒，应用程序为了ACTION_FOUND动作需要注册一个BroadcastReceiver来接受设备扫描到的信息。对于每一个设备，系统都会广播ACTION_FOUND动作。

5. 使能被发现
如果你想使你的设备能够被其他设备发现，将ACTION_REQUEST_DISCOVERABLE动作封装在intent中并调用startActivityForResult(Intent, int)方法就可以了。它将在不使你应用程序退出的情况下使你的设备能够被发现。缺省情况下的使能时间是120秒，当然你可以可以通过添加EXTRA_DISCOVERABLE_DURATION字段来改变使能时间（最大不超过300秒)

6. 连接设备
在应用程序中，想建立两个蓝牙设备之间的连接，必须实现客户端和服务器端的代码（因为任何一个设备都必须可以作为服务端或者客户端）。一个开启服务来监听，一个发起连接请求（使用服务器端设备的MAC地址）。当他们都拥有一个蓝牙套接字在同一RFECOMM信道上的时候，可以认为他们之间已经连接上了。服务端和客户端通过不同的方式或其他们的蓝牙套接字。当一个连接监听到的时候，服务端获取到蓝牙套接字。当客户可打开一个FRCOMM信道给服务器端的时候，客户端获取到蓝牙套接字。 
注意：在此过程中，如果两个蓝牙设备还没有配对好的，android系统会通过一个通知或者对话框的形式来通知用户。RFCOMM连接请求会在用户选择之前阻塞。

7. 服务端的连接
首先通过调用listenUsingRfcommWithServiceRecord(String, UUID)方法来获取BluetoothServerSocket对象，参数String代表了该服务的名称，UUID代表了和客户端连接的一个标识（128位格式的字符串ID，相当于PIN码），UUID必须双方匹配才可以建立连接。 
其次调用accept（）方法来监听可能到来的连接请求，当监听到以后，返回一个连接上的蓝牙套接字BluetoothSocket。 
最后，在监听到一个连接以后，需要调用close（）方法来关闭监听程序。（一般蓝牙设备之间是点对点的传输） 
注意：accept（）方法不应该放在主Acitvity里面，因为它是一种阻塞调用（在没有监听到连接请求之前程序就一直停在那里）。解决方法是新建一个线程来管理。例如：

8. 客户端的连接
为了初始化一个与远端设备的连接，需要先获取代表该设备的一个BluetoothDevice对象。

9. 管理连接
当设备连接上以后，每个设备都拥有各自的BluetoothSocket。就可以实现设备之间数据的共享了。 
首先通过调用getInputStream()和getOutputStream()方法来获取输入输出流。 
然后通过调用read(byte[]) 和write(byte[]).方法来读取或者写数据。 

10. 权限设置
<uses-permissionandroid:name="android.permission.BLUETOOTH_ADMIN" /><uses-permissionandroid:name="android.permission.BLUETOOTH" />
