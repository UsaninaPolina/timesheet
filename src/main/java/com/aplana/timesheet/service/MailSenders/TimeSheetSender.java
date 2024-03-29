package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.MailUtils;
import com.aplana.timesheet.util.TimeSheetUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class TimeSheetSender extends MailSender {

    private TimeSheetForm tsForm;

    public TimeSheetSender(SendMailService sendMailService) {
        super(sendMailService);
    }

    @Autowired
    protected void initFromAddresses() {
        Integer employeeId = tsForm.getEmployeeId();
        String employeeEmail = sendMailService.employeeService.find(employeeId).getEmail();
        logger.debug("From Address = {}", employeeEmail);
        try {
            fromAddr = new InternetAddress(employeeEmail);
        } catch (AddressException e) {
            logger.error("Employee email address has wrong format.", e);
        }
    }

    @Override
    protected void initCcAddresses() {
        StringBuilder ccAddresses = new StringBuilder();

        ccAddresses.append(sendMailService.getEmployeeEmail(tsForm.getEmployeeId())).append(",");
        ccAddresses.append(sendMailService.getEmployeesManagersEmails(tsForm.getEmployeeId()));
        logger.debug("EmployeesManagersEmails: {}", ccAddresses.toString());
        ccAddresses.append(sendMailService.getProjectsManagersEmails(tsForm));
        logger.debug(" + ProjectsManagersEmail: {}", ccAddresses.toString());
        ccAddresses.append(sendMailService.getProjectParticipantsEmails(tsForm.getEmployeeId(), tsForm));
        logger.debug(" + ProjectParticipantsEmails: {}", ccAddresses.toString());
        String uniqueSendingEmails = MailUtils.deleteEmailDublicates(ccAddresses.toString());
        try {
            ccAddr = InternetAddress.parse(uniqueSendingEmails);
        } catch (AddressException e) {
            logger.error("Email address has wrong format.", e);
        }
    }

    @Override
    protected void initToAddresses() {
        String toAddress = sendMailService.mailConfig.getProperty("mail.pcg.toaddress");
        try {
            toAddr = InternetAddress.parse(toAddress);
            logger.debug("To Address = {}", toAddress);
        } catch (AddressException e) {
            logger.error("Email address {} has wrong format.", toAddress, e);
        }
    }

    @Override
    protected void initMessageSubject() {
        String calDate = tsForm.getCalDate();
        String beginLongDate = tsForm.getBeginLongDate();
        String date;
        StringBuilder messageSubject = new StringBuilder();
        messageSubject.append("Status report - ");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

        if (!tsForm.isLongIllness() && !tsForm.isLongVacation()) {
            date = sdf.format(DateTimeUtil.stringToDate(calDate, "yyyy-MM-dd"));//DateTimeUtil.formatDateString(calDate);
        } else {
            date = sdf.format(DateTimeUtil.stringToDate(beginLongDate, "yyyy-MM-dd"));//DateTimeUtil.formatDateString(beginLongDate);
        }
        messageSubject.append(date);
        logger.debug("Message subject: {}", messageSubject.toString());
        try {
            message.setSubject(messageSubject.toString(), "UTF-8");
        } catch (MessagingException e) {
            logger.error("Error while init message subject.", e);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void initMessageBody() {
        Map model = new HashMap();
        TimeSheetUser securityPrincipal = sendMailService.securityService.getSecurityPrincipal();

        model.put("tsForm", tsForm);
        model.put("dictionaryItemService", sendMailService.dictionaryItemService);
        model.put("projectService", sendMailService.projectService);
        model.put("DateTimeUtil", DateTimeUtil.class);
        model.put("senderName", securityPrincipal.getEmployee().getName());
        logger.info("follows initialization output from velocity");
        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "sendmail.vm", model);
        logger.debug("Message Body: {}", messageBody);
        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    public void sendTimeSheetMessage(TimeSheetForm form) {

        tsForm = form;

        try {
            initSender();

            logger.info("Performing timesheet mailing.");

            message = new MimeMessage(session);
            initMessageHead();
            initMessageBody();

            sendMessage();

        } catch (NoSuchProviderException e) {
            logger.error("Provider for {} protocol not found.",
                    sendMailService.mailConfig.getProperty("mail.transport.protocol"), e);
        } catch (MessagingException e) {
            logger.error("Error while sending email message.", e);
        } finally {
            deInitSender();
        }

    }
}
