package be.jyl.services;

import be.jyl.entities.Accounts;
import be.jyl.tools.EMF;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.List;

public class AccountService {
    private Logger log = Logger.getLogger(AccountService.class);
    private EntityManager em = EMF.getEM();
    private EntityTransaction transaction = em.getTransaction();
    public List<Accounts> getAccounts (){
        Query query = em.createNamedQuery("Account.findAll",Accounts.class);
        return query.getResultList();
    }
    public Accounts getConnectionLogin(String pLogin, String pPassword){
        Query query= em.createNamedQuery("Account.login",Accounts.class)
                .setParameter("pLogin",pLogin)
                .setParameter("pPassword",pPassword);
        Accounts myAccount = null;
        try{
            myAccount = (Accounts) query.getSingleResult();
        }
        catch (Exception e){
            myAccount = null;
        }finally {
            return myAccount;
        }
    }
}
