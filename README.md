1. 启动蓝牙功能
首先通过调用静态方法getDefaultAdapter()获取蓝牙适配器BluetoothAdapter，如果返回为空，则无法继续执行了。例如：
BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();if (mBluetoothAdapter == null) {   // Device does not support Bluetooth}
其次，调用isEnabled()来查询当前蓝牙设备的状态，如果返回为false，则表示蓝牙设备没有开启，接下来你需要封装一个ACTION_REQUEST_ENABLE请求到intent里面，调用startActivityForResult()方法使能蓝牙设备，例如：
if (!mBluetoothAdapter.isEnabled()) {    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);}

2. 查找设备
使用BluetoothAdapter类里的方法，你可以查找远端设备（大概十米以内）或者查询在你手机上已经匹配（或者说绑定）的其他设备了。当然需要确定对方蓝牙设备已经开启或者已经开启了“被发现使能”功能（对方设备是可以被发现的是你能够发起连接的前提条件）。如果该设备是可以被发现的，会反馈回来一些对方的设备信息，比如名字、MAC地址等，利用这些信息，你的设备就可以选择去向对方初始化一个连接。 
如果你是第一次与该设备连接，那么一个配对的请求就会自动的显示给用户。当设备配对好之后，他的一些基本信息（主要是名字和MAC）被保存下来并可以使用蓝牙的API来读取。使用已知的MAC地址就可以对远端的蓝牙设备发起连接请求。 
匹配好的设备和连接上的设备的不同点：匹配好只是说明对方设备发现了你的存在，并拥有一个共同的识别码，并且可以连接。连接上：表示当前设备共享一个RFCOMM信道并且两者之间可以交换数据。也就是是说蓝牙设备在建立RFCOMM信道之前，必须是已经配对好了的。

3. 查询匹配好的设备
在建立连接之前你必须先查询配对好了的蓝牙设备集以便选取一个设备进行通信，例如你可以你可以查询所有配对的蓝牙设备，并使用一个数组适配器将其打印显示出来：
Set<BluetoothDevice> pairedDevices =mBluetoothAdapter.getBondedDevices();// If there are paired devicesif (pairedDevices.size() > 0) {    //Loop through paired devices    for (BluetoothDevice device : pairedDevices) {        // Add the name and address to an array adapter to show in a ListView        mArrayAdapter.add(device.getName() + "/n" + device.getAddress());    }}
建立一个蓝牙连接只需要MAC地址就已经足够。

4. 扫描设备
扫描设备，只需要调用startDiscovery()方法，这个扫描的过程大概持续是12秒，应用程序为了ACTION_FOUND动作需要注册一个BroadcastReceiver来接受设备扫描到的信息。对于每一个设备，系统都会广播ACTION_FOUND动作。
// 用ACTION_FOUND为action注册广播接收器private final BroadcastReceiver mReceiver = new BroadcastReceiver() {    public void onReceive(Context context, Intent intent) {        String action = intent.getAction();        // 发现设备        if (BluetoothDevice.ACTION_FOUND.equals(action)) {        // 从Intent中获取蓝牙设备        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);        // 添加名字和地址到设备适配器中        mArrayAdapter.add(device.getName() + "/n" + device.getAddress());        }    }};//注册广播接收器IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);registerReceiver(mReceiver, filter); //在onDestroy时记得注销广播接收器

5. 使能被发现
如果你想使你的设备能够被其他设备发现，将ACTION_REQUEST_DISCOVERABLE动作封装在intent中并调用startActivityForResult(Intent, int)方法就可以了。它将在不使你应用程序退出的情况下使你的设备能够被发现。缺省情况下的使能时间是120秒，当然你可以可以通过添加EXTRA_DISCOVERABLE_DURATION字段来改变使能时间（最大不超过300秒)例如：
Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);startActivity(discoverableIntent);
运行该段代码之后，系统会弹出一个对话框来提示你启动设备使能被发现（此过程中如果你的蓝牙功能没有开启，系统会帮你开启），并且如果你准备对该远端设备发现一个连接，你不需要开启设备被发现功能，因为该功能只是在你的应用程序作为服务器端的时候才需要。

6. 连接设备
在应用程序中，想建立两个蓝牙设备之间的连接，必须实现客户端和服务器端的代码（因为任何一个设备都必须可以作为服务端或者客户端）。一个开启服务来监听，一个发起连接请求（使用服务器端设备的MAC地址）。当他们都拥有一个蓝牙套接字在同一RFECOMM信道上的时候，可以认为他们之间已经连接上了。服务端和客户端通过不同的方式或其他们的蓝牙套接字。当一个连接监听到的时候，服务端获取到蓝牙套接字。当客户可打开一个FRCOMM信道给服务器端的时候，客户端获取到蓝牙套接字。 
注意：在此过程中，如果两个蓝牙设备还没有配对好的，android系统会通过一个通知或者对话框的形式来通知用户。RFCOMM连接请求会在用户选择之前阻塞。

7. 服务端的连接
当你想要连接两台设备时，一个必须作为服务端（通过持有一个打开的BluetoothServerSocket），目的是监听外来连接请求，当监听到以后提供一个连接上的BluetoothSocket给客户端，当客户端从BluetoothServerSocket得到BluetoothSocket以后就可以销毁BluetoothServerSocket，除非你还想监听更多的连接请求。 
建立服务套接字和监听连接的基本步骤： 
首先通过调用listenUsingRfcommWithServiceRecord(String, UUID)方法来获取BluetoothServerSocket对象，参数String代表了该服务的名称，UUID代表了和客户端连接的一个标识（128位格式的字符串ID，相当于PIN码），UUID必须双方匹配才可以建立连接。 
其次调用accept（）方法来监听可能到来的连接请求，当监听到以后，返回一个连接上的蓝牙套接字BluetoothSocket。 
最后，在监听到一个连接以后，需要调用close（）方法来关闭监听程序。（一般蓝牙设备之间是点对点的传输） 
注意：accept（）方法不应该放在主Acitvity里面，因为它是一种阻塞调用（在没有监听到连接请求之前程序就一直停在那里）。解决方法是新建一个线程来管理。例如：

8. 客户端的连接
为了初始化一个与远端设备的连接，需要先获取代表该设备的一个BluetoothDevice对象。通过BluetoothDevice对象来获取BluetoothSocket并初始化连接，具体步骤： 
使用BluetoothDevice对象里的方法createRfcommSocketToServiceRecord(UUID)来获取BluetoothSocket。UUID就是匹配码。然后，调用connect（）方法来。如果远端设备接收了该连接，他们将在通信过程中共享RFFCOMM信道，并且connect返回。 
注意：conncet（）方法也是阻塞调用，一般建立一个独立的线程中来调用该方法。在设备discover过程中不应该发起连接connect（），这样会明显减慢速度以至于连接失败。且数据传输完成只有调用close（）方法来关闭连接，这样可以节省系统内部资源。

9. 管理连接
当设备连接上以后，每个设备都拥有各自的BluetoothSocket。就可以实现设备之间数据的共享了。 
首先通过调用getInputStream()和getOutputStream()方法来获取输入输出流。 
然后通过调用read(byte[]) 和write(byte[]).方法来读取或者写数据。 
实现细节：以为读取和写操作都是阻塞调用，需要建立一个专用线程来管理。

10. 权限设置
<uses-permissionandroid:name="android.permission.BLUETOOTH_ADMIN" /><uses-permissionandroid:name="android.permission.BLUETOOTH" />
