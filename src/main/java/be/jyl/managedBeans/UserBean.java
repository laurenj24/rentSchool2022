package be.jyl.managedBeans;

import be.jyl.entities.*;
import be.jyl.enums.ResponsibleType;
import be.jyl.services.UsersService;
import be.jyl.services.BorrowersService;
import be.jyl.tools.NotificationManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Named
@SessionScoped
public class UserBean implements Serializable {
    private Logger log = Logger.getLogger(UserBean.class);
    private Users user;
    private List<Roles> rolesList;
    private String borrowerSearchText;
    private BorrowersService borrowersService = new BorrowersService();
    private UsersService usersService = new UsersService();
    private List<Cities> citiesList;
    private List<Borrowers> listBorrowers;
    private List<Users> filteredUser;
    private Users userSession;
    private Borrowers borrowerSelected;
    private Borrowers newBorrower;
    private Users newUser;
    private List<Users> usersList;
    private String userSearch;
    private Users userSelected;
    private String newPassword;
    private String newPasswordRepeat;
    private boolean isAnUpdate;

    @PostConstruct
    public void init(){
        FacesContext context = FacesContext.getCurrentInstance();
        this.userSession = (Users) context.getExternalContext().getSessionMap().get("userSession") ;
        this.citiesList = borrowersService.listCities();
        this.rolesList = usersService.listRoles();

        user = new Users();
    }
    /**----------------------------------------------------
     * La UserBean gère les Emprunteurs et les Utilisateurs
     * @jiwaii CRUDs
    -------------------------------------------------------*/

    /** BORROWERS **/

    /**
     * updateOrInsertBorrower est une fonction inteligente qui déférencie s'il s'agit
     * d'une mise à jour ou d'un nouvel emprunteur (meme principe pour les Users plus bas).
     * Elle se sert de la variable isAnUpdate qui est affecté en :
     * TRUE : si l'utilisateur clique sur le bouton modifier
     * FALSE : si on clique sur le bouton ajouter un emprunteur
     * */
    public String updateOrInsertBorrower(){
        log.log(Level.INFO,"updateOrInsertBorrower()");
        Pattern pattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        Matcher matcher = pattern.matcher(borrowerSelected.getEmail());
        if (matcher.matches()){
            /** TRANSACTION **/
            try{
                if (!borrowersService.em.isOpen()) borrowersService = new BorrowersService();
                borrowersService.transaction.begin();
                if(isAnUpdate == false){
                    /** c'est un nouvelle emprunteur , on le persist : */
                    borrowersService.em.persist(borrowerSelected);
                    log.log(Level.INFO,"PERSIST");
                }
                borrowersService.transaction.commit();
                //NOTIFICATION SUCCES
                PrimeFaces.current().executeScript("PF('manageUserDialog').hide()");
                PrimeFaces.current().ajax().update("form:messages", "form:dt-users");
                NotificationManager.addInfoMessage("notification.users.useradded");
            }
            finally {
                if (borrowersService.transaction.isActive()){
                    borrowersService.transaction.rollback();
                    //NOTIFICATION FAIL
                    PrimeFaces.current().ajax().update("form:messages", "");
                    FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,null,"Email invalide !"));
                    NotificationManager.addErrorMessage("notification.users.error");
                }
            }
        }
        else {
            //NOTIFICATION FAIL E-MAIL
            PrimeFaces.current().ajax().update("form:messages", "");
            FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,null,"Email invalide !"));
            NotificationManager.addErrorMessage("notification.users.error");
        }
        listBorrowers = borrowersService.listBorrowers();
        return "borrowersList";
    }
    public String deleteBorrower(){
        {
            try {
                borrowersService.transaction.begin();
                borrowersService.em.remove(borrowerSelected);
                borrowersService.transaction.commit();
                NotificationManager.addInfoMessage("notification.borrowerDeleted");
            } finally {
                if (borrowersService.transaction.isActive()) {
                    borrowersService.transaction.rollback();
                    NotificationManager.addErrorMessage("notification.users.error");
                }
            }
            listBorrowers = borrowersService.listBorrowers();
        }
        return "borrowersList";
    }
    //Ouvrir la modal et initialiser les champs:
    public void openDialogForNewBorrower() {
        log.log(Level.INFO,"openDialogForNewBorrower");
        this.isAnUpdate = false;
        this.borrowerSelected = new Borrowers();
        this.citiesList = borrowersService.listCities();
    }
    public void openDialogForUpdateBorrower(){
        log.log(Level.INFO,"openDialogForUpdateBorrower");
        this.isAnUpdate = true;
    }
    public boolean isAnBorrowerUsed(Borrowers borrower){
        return borrowersService.isAnUsedBorrower(borrower);
    }

    /** USERS **/
    public String updateOrInsertUser(){
        log.log(Level.INFO,userSelected.getLastname());
        if(!usersService.em.isOpen())usersService = new UsersService();
        if (isAnUpdate == false){
            /** Nouvelle utilisateur */

            if (!newPassword.equals(newPasswordRepeat)) {
                PrimeFaces.current().ajax().update("form:messages", "form:dt-users");
                NotificationManager.addErrorMessage("notification.passwordNoMatch");
                return "";
            }else{
                try{
                    usersService.transaction.begin();
                    log.log(Level.INFO,"PERSIST User");
                    userSelected.setPassword(usersService.hashingPassword(newPassword));
                    userSelected.setResponsibleType(ResponsibleType.staff);
                    usersService.em.persist(userSelected);
                    usersService.transaction.commit();

                    PrimeFaces.current().executeScript("PF('manageUserDialog').hide()");
                    PrimeFaces.current().ajax().update("form:messages", "form:dt-users");
                    NotificationManager.addInfoMessage("notification.userUpdated");
                }
                finally {
                    if(usersService.transaction.isActive()){
                        usersService.transaction.rollback();
                    }
                }
            }
        }else
        {
            /** mise à jour utilisateur */
            try{
                usersService.transaction.begin();
                usersService.transaction.commit();

                PrimeFaces.current().executeScript("PF('manageUserDialog').hide()");
                PrimeFaces.current().ajax().update("form:messages", "form:dt-users");
                NotificationManager.addInfoMessage("notification.userUpdated");
            }
            finally {
                if(usersService.transaction.isActive()){
                    usersService.transaction.rollback();
                }
            }
        }
        usersList = usersService.listUsers();
        return "usersList";
    }
    public void resetPassword(){

    }
    /** L'utilisateur se trouve dans une location ? * */
    public boolean isAnUserUsed(Users user){
        return usersService.isAnUsedUser(user);
    }
    public String deleteUser(){
        if(userSelected.getLogin().toLowerCase().equals("admin")){
            NotificationManager.addErrorMessage("notification.users.cannotdeletethisuser");
        }else
        {
            try {
                usersService.transaction.begin();
                usersService.em.remove(userSelected);
                usersService.transaction.commit();
                NotificationManager.addInfoMessage("notification.userDeleted");
            } finally {
                if (usersService.transaction.isActive()) {
                    usersService.transaction.rollback();
                }
            }
            usersList = usersService.listUsers();
        }
        return "usersList";
    }

    public void openDialogForUpdateUser(){
        isAnUpdate = true;
    }
    public void openDialogForNewUser(){
        userSelected = new Users();
        isAnUpdate = false;
        citiesList = borrowersService.listCities();
    }

    // Si loging existe déjà
    public boolean loginExist(){
        log.log(Level.INFO,"loginExist()");
        return usersService.userExist("%"+ userSelected.getLogin()+"%");
    }

    /**-----------------------------
     * Navigation Pages
     -----------------------------*/

    /** Page pour ajouter un utilisateur
     * et Chargement des villes
     *
     * @return String nom de la jsf
     */
    public String addUserPage(){
        citiesList = borrowersService.listCities();
        return "userAdd";
    }

    public String listUserAccountsPage(){
        usersList = usersService.listUsers();
        return "accountUserList";
    }

    /** Page pour lister les utilisateurs
     *  Chargement des utilisateur
     *
     * @return String nom de la jsf
     */
    public String listBorrowersPage(){
        listBorrowers = borrowersService.listBorrowers();
        return "borrowersList";
    }
    public String listUsersPage(){
        usersList = usersService.listUsers();
        return "usersList";
    }
    public void dtUserSelection(SelectEvent selectEvent){
        borrowerSelected = (Users) selectEvent.getObject();
        log.log(Level.INFO, borrowerSelected.getFirstname()+" Selected");
    }
    public void inputSearchBorrower(){
        log.log(Level.INFO,"inputSearchBorrower() : UserBean.userSeachText is = "+ borrowerSearchText);
        listBorrowers = borrowersService.listBorrowers(borrowerSearchText);
    }
    public void inputSearchBorrowerToUser(){
        listBorrowers = borrowersService.listBorrowers(borrowerSearchText);
    }
    public void usersLoad(){
        usersList = usersService.listUsers(userSearch);
    }

    /** ------------------
     * GETTERS AND SETTERS
     ----------------------*/
//<editor-fold desc="Getters Setters">
    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public List<Cities> getCitiesList() {
        return citiesList;
    }

    public void setCitiesList(List<Cities> citiesList) {
        this.citiesList = citiesList;
    }

    public List<Borrowers> getListBorrowers() {
        return listBorrowers;
    }

    public void setListBorrowers(List<Borrowers> listBorrowers) {
        this.listBorrowers = listBorrowers;
    }

    public String getBorrowerSearchText() {
        return borrowerSearchText;
    }

    public void setBorrowerSearchText(String borrowerSearchText) {
        this.borrowerSearchText = borrowerSearchText;
    }

    public Borrowers getBorrowerSelected() {
        return borrowerSelected;
    }

    public void setBorrowerSelected(Borrowers borrowerSelected) {
        this.borrowerSelected = borrowerSelected;
    }

    public List<Users> getFilteredUser() {
        return filteredUser;
    }

    public void setFilteredUser(List<Users> filteredUser) {
        this.filteredUser = filteredUser;
    }

    public Users getNewUser() {
        return newUser;
    }

    public void setNewUser(Users newUser) {
        this.newUser = newUser;
    }

    public List<Roles> getRolesList() {
        return rolesList;
    }

    public void setRolesList(List<Roles> rolesList) {
        this.rolesList = rolesList;
    }

    public List<Users> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<Users> usersList) {
        this.usersList = usersList;
    }

    public String getUserSearch() {
        return userSearch;
    }

    public void setUserSearch(String userSearch) {
        this.userSearch = userSearch;
    }

    public Users getUserSelected() {
        return userSelected;
    }

    public void setUserSelected(Users userSelected) {
        this.userSelected = userSelected;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPasswordRepeat() {
        return newPasswordRepeat;
    }

    public void setNewPasswordRepeat(String newPasswordRepeat) {
        this.newPasswordRepeat = newPasswordRepeat;
    }

    public Borrowers getNewBorrower() {
        return newBorrower;
    }

    public void setNewBorrower(Borrowers newBorrower) {
        this.newBorrower = newBorrower;
    }

    public boolean getIsAnUpdate() {
        return this.isAnUpdate;
    }

    public void setIsAnUpdate(boolean isAnUpdate) {
        this.isAnUpdate = isAnUpdate;
    }
    //</editor-fold>
}
