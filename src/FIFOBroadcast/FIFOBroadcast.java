package FIFOBroadcast;

import UniformReliableBroadcast.UniformReliableBroadcast;
import Models.Message;
import Process.Process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;


public class FIFOBroadcast {

    private volatile static FIFOBroadcast fifoBroadcast = new FIFOBroadcast();
    private UniformReliableBroadcast uniformReliableBroadcast;
    int lsn;
    HashMap<Message, Integer> pending;
    int next[];

    private FIFOBroadcast() {
        uniformReliableBroadcast = new UniformReliableBroadcast();
        lsn = 0;
        pending = new HashMap<>();
        int numberOfProcesses = Process.getInstance().processes.size();
        next = new int[numberOfProcesses + 1];
        for (int i = 0; i <= numberOfProcesses; ++i)
            next[i] = 1;
    }

    public static FIFOBroadcast getInst() {
        return fifoBroadcast;
    }


    public synchronized void Broadcast(int content) {

        lsn++;
        System.out.println("b " + lsn);
        uniformReliableBroadcast.Broadcast(content, lsn);
        Process.getInstance().Logger.WriteToLog("b " + lsn);
    }

    public synchronized void Deliver(Message message, Message originalMessage, int content, int portReceived, InetAddress addressReceived, int fifoId) throws IOException {

        if (uniformReliableBroadcast.Deliver(message, originalMessage, content, portReceived, addressReceived, fifoId)) {
            int originalProcessId = originalMessage.getProcessId();
            Message fifoMessage = new Message(fifoId, originalProcessId, content);
            pending.put(fifoMessage, originalMessage.getMessageId());
            while (true) {
                int nextId = next[originalProcessId];
                Message fifoKey = new Message(nextId, originalProcessId, content);
                if (pending.containsKey(fifoKey)) {
                    pending.remove(fifoKey);
                    System.out.println("d " + originalMessage.getProcessId() + " " + next[originalProcessId]);
                    //System.out.println("FIFO: " + Process.getInstance().Id + " Message #" + message.getMessageId() + ":From Process: " + originalMessage.getProcessId() + " is delivered");
                    Process.getInstance().Logger.WriteToLog("d " + originalMessage.getProcessId() + " " + next[originalProcessId]);
                    next[originalProcessId]++;
                } else break;
            }
        }
    }
}