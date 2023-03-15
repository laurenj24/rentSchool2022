package be.jyl.services;

import be.jyl.entities.Categories;
import be.jyl.entities.Reminders;
import be.jyl.tools.EMF;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class RemindersService {
    private Logger log = Logger.getLogger(RemindersService.class);
    private EntityManager em = EMF.getEM();
    private EntityTransaction transaction = em.getTransaction();

    public List<Reminders> getAllReminders() {
        Query query = em.createNamedQuery("Reminders.findAll");
        return query.getResultList();
    }

    public void addReminder(Reminders reminder) {
        Date actualDate = Date.valueOf(LocalDate.now());

        transaction.begin();
        reminder.setReminderDate(actualDate);
        log.log(Level.INFO, "userid " +reminder.getIdReminder());
        log.log(Level.INFO, "message " +reminder.getMessage());
        log.log(Level.INFO, "rentalid " +reminder.getRentalsByIdRental().getIdRental());
        em.persist(reminder);
        transaction.commit();
    }

}