/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loanbroker.ws;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import loanbroker.dto.LoanRequest;
import loanbroker.dto.LoanResponse;
import loanbroker.messaging.Messaging;

/**
 *
 * @author Kaboka
 */
@Stateless
@WebService(serviceName = "LoanBrokerWS")
public class LoanBrokerWS {



    @WebMethod(operationName = "getLoanRequest")
    public LoanResponse getLoanRequest(@WebParam(name = "request") LoanRequest request) {
        Messaging message = new Messaging();
        String msg = "<LoanRequest>\n" +
                "<ssn>" + request.ssn + "</ssn>\n" +
                "<loanAmount>" + request.loanAmount + "</loanAmount>\n" +
                "<loanDuration>" + request.loanDuration + "</loanDuration>\n" + 
                "</LoanRequest>\n";
        return message.getLoanRequest(msg);
    }
}
