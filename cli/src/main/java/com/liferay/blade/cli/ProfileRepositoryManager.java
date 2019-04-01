package com.liferay.blade.cli;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author Christopher Bryan Boyd
 */
public class ProfileRepositoryManager {

	public static void saveProfileRepository(ProfileRepository profileRepository, Path path) {
	    try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ProfileRepository.class);
		    
		    Marshaller marshaller = jaxbContext.createMarshaller();
		 
		    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		    
		    marshaller.marshal(profileRepository, System.out);
		    
		    File file = path.toFile();
		    
		    marshaller.marshal(profileRepository, file);
	    }
	    catch (JAXBException e) {
	    	throw new RuntimeException("Saving ProfileRepository failed", e);
	    }
	}
	
	public static ProfileRepository loadProfileRepository(Path path) {
		try {
			if (Files.exists(path)) {
			    JAXBContext jaxbContext = JAXBContext.newInstance(ProfileRepository.class);
			    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			   
			    ProfileRepository profileRepository = (ProfileRepository) jaxbUnmarshaller.unmarshal(path.toFile());
			     
		    	return profileRepository;
			}
			else {
				return new ProfileRepository();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
