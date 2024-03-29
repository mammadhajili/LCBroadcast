package Listener;

import AppSettings.ApplicationSettings;
import BestEffordBroadcast.BestEffortBroadcast;
import Enums.ProtocolTypeEnum;
import FIFOBroadcast.FIFOBroadcast;
import LocalCausalBroadcast.LocalCausalBroadcast;
import Models.MessageModel;
import PerfectLink.PerfectLink;
import UniformReliableBroadcast.UniformReliableBroadcast;
import Process.Process;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.concurrent.*;

public class Listener {
    private DatagramSocket socketIn;

    private PerfectLink perfectLink;
    private BestEffortBroadcast bestEffortBroadcast;
    private UniformReliableBroadcast uniformReliableBroadcast;
    private FIFOBroadcast fifoBroadcast;
    private LocalCausalBroadcast localCausalBroadcast;


    public Listener() {
        //System.out.println("Listening ...");
        this.perfectLink = PerfectLink.getInst();
        this.bestEffortBroadcast = BestEffortBroadcast.getInst();
        this.uniformReliableBroadcast = UniformReliableBroadcast.getInst();
        this.fifoBroadcast = FIFOBroadcast.getInst();
        this.localCausalBroadcast = LocalCausalBroadcast.getInst();
    }

    public void Start() throws IOException {

        socketIn = new DatagramSocket(Process.getInstance().Port);
        byte[] packetReceived = new byte[1024];
        InetAddress addressReceived;
        byte[] messageReceived;
        int portReceived;

        ThreadPoolExecutor  threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(ApplicationSettings.getInstance().ListenerThreadPoolSize);
        threadPool.prestartCoreThread();
        while (true) {
            //receiving packet
            DatagramPacket receivedPacket = new DatagramPacket(packetReceived, packetReceived.length);
            socketIn.receive(receivedPacket);

            addressReceived = receivedPacket.getAddress();
            portReceived = receivedPacket.getPort();
            messageReceived = receivedPacket.getData();

            threadPool.submit(new RequestProcessing(Arrays.copyOf(messageReceived, messageReceived.length), portReceived, InetAddress.getByName(addressReceived.getHostAddress())));
        }
    }

    public class RequestProcessing extends Thread {
        byte[] messageReceived;
        int portReceived;
        InetAddress addressReceived;

        public RequestProcessing(final byte[] messageReceived, final int portReceived, final InetAddress addressReceived) {
            this.messageReceived = messageReceived;
            this.portReceived = portReceived;
            this.addressReceived = addressReceived;
        }

        @Override
        public void run() {
            IntBuffer intBuf =
                    ByteBuffer.wrap(messageReceived)
                            .order(ByteOrder.BIG_ENDIAN)
                            .asIntBuffer();

            int[] messageArray = new int[intBuf.remaining()];
            intBuf.get(messageArray);

            //PRE PROCESS THE MESSAGE
            int messageId = messageArray[0];
            int protocol = messageArray[1];
            int content = messageArray[2];
            int processId = messageArray[3];
            MessageModel message = new MessageModel(messageId, processId);
            this.setName("Send Thread " + messageId);

            // DELIVER THE MESSAGE ACCORDING TO PROTOCOL
            try {
                if (protocol == ProtocolTypeEnum.PerfectLink.ordinal()) {
                    perfectLink.Deliver(message, content, portReceived, addressReceived);
                } else if (protocol == ProtocolTypeEnum.BestEffortBroadcast.ordinal()) {
                    bestEffortBroadcast.Deliver(message, content, portReceived, addressReceived);
                } else if (protocol == ProtocolTypeEnum.UniformReliableBroadcast.ordinal()) {
                    int originalProcessId = messageArray[4];
                    int originalMessageId = messageArray[5];
                    MessageModel messageOriginal = new MessageModel(originalMessageId, originalProcessId);

                    uniformReliableBroadcast.Deliver(message, messageOriginal, content, portReceived, addressReceived, 0);
                } else if (protocol == ProtocolTypeEnum.FIFOBroadcast.ordinal()) {
                    int originalProcessId = messageArray[4];
                    int originalMessageId = messageArray[5];
                    MessageModel messageOriginal = new MessageModel(originalMessageId, originalProcessId);
                    int fifoId = messageArray[6];

                    fifoBroadcast.Deliver(message, messageOriginal, content, portReceived, addressReceived, fifoId);
                } else if (protocol == ProtocolTypeEnum.LocalCausalBroadcast.ordinal()) {

                    int originalProcessId = messageArray[4];
                    int originalMessageId = messageArray[5];
                    MessageModel messageOriginal = new MessageModel(originalMessageId, originalProcessId);

                    int numOfProcesses = Process.getInstance().processes.size();
                    int [] vectorClock = new int[numOfProcesses + 1];
                    for (int j= 0; j < numOfProcesses + 1; j++){
                        vectorClock[j] = messageArray[7 + j];
                    }

                    localCausalBroadcast.Deliver(message, messageOriginal, content, portReceived, addressReceived, vectorClock);

                }else {
                    System.out.println("Unknown protocol " + protocol);
                    return;
                }
            } catch (IOException e) {
                System.out.println("RequestProcessing error: " + e);
            }
        }
    }
}
