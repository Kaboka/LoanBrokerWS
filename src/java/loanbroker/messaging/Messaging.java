/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loanbroker.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import loanbroker.dto.LoanResponse;
import org.w3c.dom.Document;
import utilities.xml.xmlMapper;

/**
 *
 * @author Kaboka
 */
public class Messaging {
    
    private static final String IN_QUEUE = "webservice_gr1"; 
    private static final String OUT_QUEUE = "enricher_creditScore_gr1";;
    private static Channel inChannel;
    private static Channel outChannel;
    private Connection con;
    private QueueingConsumer consumer;        
   
    public Messaging(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("student");
        factory.setPassword("cph");
        factory.setHost("datdb.cphbusiness.dk");
        
        Connection connection;
        try {
            con = factory.newConnection();
            inChannel = con.createChannel();
            inChannel.queueDeclare(IN_QUEUE, false, false, false, null);
            outChannel= con.createChannel();
            outChannel.queueDeclare(OUT_QUEUE, false, false, false, null);
            consumer = new QueueingConsumer(inChannel);
            inChannel.basicConsume(IN_QUEUE,false, consumer);
        } catch (IOException ex) {
            Logger.getLogger(Messaging.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public LoanResponse getLoanRequest(String message){
       LoanResponse response = null;
        try {
            publishMessage(message);
            response = consumeMessage();
        } catch (IOException ex) {
            Logger.getLogger(Messaging.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Messaging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
    private void publishMessage(String message) throws IOException{
        outChannel.basicPublish("", OUT_QUEUE, null, message.getBytes());
    }
    
    
    private LoanResponse consumeMessage() throws InterruptedException, IOException {
        Delivery delivery = consumer.nextDelivery();
        System.err.println("DELIVERY GETBODY: " + new String(delivery.getBody()));
        inChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        con.close();
        LoanResponse response = new LoanResponse();
        Document doc = xmlMapper.getXMLDocument(new String(delivery.getBody()));
        XPath xPath = XPathFactory.newInstance().newXPath();
        String ssn = "";
        String bankName = "";
        double interestRate = 0.0;
        try {
            bankName = xPath.compile("/LoanResponse/bankName").evaluate(doc);
            ssn = xPath.compile("/LoanResponse/ssn").evaluate(doc);
            interestRate = Double.parseDouble(xPath.compile("/LoanResponse/intrestRate").evaluate(doc));
            response.interrestRate = interestRate;
            response.bankName = bankName;
            response.ssn = ssn;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(Messaging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
}
