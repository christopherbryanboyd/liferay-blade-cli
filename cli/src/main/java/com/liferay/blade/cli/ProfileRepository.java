package com.liferay.blade.cli;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "profiles")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileRepository {
    @XmlElement(name = "profile")
    private List<ProfileEntry> _profileEntries = null;
 
    public List<ProfileEntry> getProfileEntries() {
        return _profileEntries;
    }
 
    public void setProfileEntries(List<ProfileEntry> employees) {
        this._profileEntries = employees;
    }
}
