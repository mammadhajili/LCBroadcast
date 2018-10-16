package com.epfl.da.PerfectLink;

import com.epfl.da.Enums.ProtocolTypeEnum;
import com.epfl.da.Interfaces.BaseHandler;
import com.epfl.da.Interfaces.MessageHandler;
import com.epfl.da.Models.Message;
import com.epfl.da.Process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;

public class PerfectLink {

    private SendEvent sendEvent;
    private DeliverEvent deliverEvent;

    private static HashSet<Message> receivedMessages;
    public MessageHandler onMessageReceive;
    public BaseHandler receiveAcknowledgeHandler;


    public PerfectLink() {
        sendEvent = new SendEvent();
        deliverEvent = new DeliverEvent();
        receivedMessages = new HashSet<>();
    }

    /** For PerfectLink */
    public void Send(int message, InetAddress destAddress, int destPort){
        var id = SendEvent.NextId();
        sendEvent.SendMessage(message, destAddress, destPort, ProtocolTypeEnum.PerfectLink, 0 , 0, id);
    }

    public void Send(int message, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int messageId){
        sendEvent.SendMessage(message, destAddress, destPort, protocol, 0 , 0, messageId);
    }
    /** For UniformReliableBroadcast */
    public void Send(int message, InetAddress destAddress, int destPort, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int messageId){
        sendEvent.receiveAcknowledgeHandler = receiveAcknowledgeHandler;
        sendEvent.SendMessage(message, destAddress, destPort, protocol, originalProcessId, originalMessageId, messageId );
    }

    public boolean Deliver(Message message, int content, int port, InetAddress address) throws IOException {

        System.out.println("perfect link processess..");
        if (receivedMessages.contains(message)) {
            System.out.println("Message #" + message.getMessageId() + ": " + content + " duplicate");
        } else {
            System.out.println("Message #" + message.getMessageId() + ": " + content + " is delivered");
            receivedMessages.add(message);
            deliverEvent.sendAck(port, address, message.getMessageId());
            return true;
        }
        return false;
    }
}

