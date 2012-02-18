package org.jboss.soa.esb.test.action;

import org.jboss.soa.esb.actions.annotation.Process;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.service.DeliveryService;
import org.jboss.soa.esb.service.InvoiceService;
import org.jboss.soa.esb.spring.actions.AbstractAutowiredSpringAction;
import org.springframework.beans.factory.annotation.Autowired;

public class TestDIAction extends AbstractAutowiredSpringAction {
	
	@Autowired
	private InvoiceService invoiceService;
	
	@Autowired
	private DeliveryService deliveryService;
	
	@Process
	public Message doSomething(Message message) {
		invoiceService.sendInvoice();
		deliveryService.createDelivery();
		return message;
	}
	

}
