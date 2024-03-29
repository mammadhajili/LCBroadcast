package BestEffordBroadcast;

import Enums.ProtocolTypeEnum;
import Models.MessageModel;
import Models.ProcessModel;
import PerfectLink.PerfectLink;
import PerfectLink.SendEvent;
import Process.Process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;


public class BestEffortBroadcast {

    private PerfectLink perfectlink = PerfectLink.getInst();
    private static volatile BestEffortBroadcast bestEffortBroadcast = new BestEffortBroadcast();

    private BestEffortBroadcast() {
    }
    public static BestEffortBroadcast getInst() {
        return bestEffortBroadcast;
    }
    public void Broadcast(int message) {

        ArrayList<ProcessModel> processes = Process.getInstance().processes;
        int id = SendEvent.NextId();
        //System.out.println("URB: " + Process.Process.getInstance().Id + " Broadcast MessageModel #" + id);
        for (int i = 0; i < processes.size(); i++) {
            perfectlink.Send(message, processes.get(i).address, processes.get(i).port, ProtocolTypeEnum.BestEffortBroadcast, id);
        }
    }

    //** For UniformReliableBroadcast *//*
    public void Broadcast(int content, int originalProcessId, int originalMessageId, ProtocolTypeEnum protocol, int messageId) {

        ArrayList<ProcessModel> processes = Process.getInstance().processes;
        for (int i = 0; i < processes.size(); i++) {
            perfectlink.Send(content, processes.get(i).address, processes.get(i).port, protocol, originalProcessId, originalMessageId, messageId);
        }
    }

    //** For FIFOBroadcast *//*
    public void Broadcast(int content, int originalProcessId, int originalMessageId, ProtocolTypeEnum protocol, int messageId, int fifoId) {

        ArrayList<ProcessModel> processes = Process.getInstance().processes;
        for (int i = 0; i < processes.size(); i++) {
            perfectlink.Send(content, processes.get(i).address, processes.get(i).port, protocol, originalProcessId, originalMessageId, messageId, fifoId);
        }
    }

    //** For LocalCausalBroadcast *//*
    public synchronized void Broadcast(int content, int originalProcessId, int originalMessageId, ProtocolTypeEnum protocol, int messageId, int[] vectorClock) {

        ArrayList<ProcessModel> processes = Process.getInstance().processes;
        for (int i = 0; i < processes.size(); i++) {
            perfectlink.Send(content, processes.get(i).address, processes.get(i).port, protocol, originalProcessId, originalMessageId, messageId, vectorClock);
        }
    }

    public boolean Deliver(MessageModel message, int content, int portReceived, InetAddress addressReceived) throws IOException {

        return perfectlink.Deliver(message, content, portReceived, addressReceived);
    }
}

