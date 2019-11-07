/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

/**
 * Page Used to display the output(Feedback) on the tableView
 * @author Ali FAKIH, Maria Afara
 * fakih.k.ali@gmail.com
 * maria-afara5@hotmail.com 
 *
 */
public class Content {

    String Informations;
    String Date;

    public Content() {

    }

    public Content(String Informations, String Date) {
        this.Informations = Informations;
        this.Date = Date;
    }

    public String getInformations() {
        return Informations;
    }

    public void setInformations(String Informations) {
        this.Informations = Informations;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String Date) {
        this.Date = Date;
    }

}
