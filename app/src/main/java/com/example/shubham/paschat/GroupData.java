package com.example.shubham.paschat;
/*
    This Class Contains The Data Variables Related To The Groups.
    It Has Two Constructors-Default And Parameterized To Create Instances Of The Class.
    It Also Contains Functions To Set And Retrieve The Values Of The Data Variables.
 */

public class GroupData {
    private String groupName;
    private String groupCreatedBy;
    private String dateCreated;

    public GroupData(){}

    GroupData(String groupName,String groupCreatedBy,String dateCreated) {
        this.groupCreatedBy = groupCreatedBy;
        this.groupName = groupName;
        this.dateCreated = dateCreated;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupCreatedBy() {
        return groupCreatedBy;
    }

    public void setGroupCreatedBy(String groupCreatedBy) {
        this.groupCreatedBy = groupCreatedBy;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {this.dateCreated = dateCreated;}
}
