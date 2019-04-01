package com.liferay.blade.cli;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="profile")
@XmlAccessorType (XmlAccessType.FIELD)
public class ProfileEntry {
	private Date updatedDate;
	private String version;
	private String profileName;
	private String githubProjectUrl;
	private String author;
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public String getGithubProjectUrl() {
		return githubProjectUrl;
	}
	public void setGithubProjectUrl(String githubProjectUrl) {
		this.githubProjectUrl = githubProjectUrl;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
}
