package com.aplana.timesheet.dao.entity.ldap;

import java.io.UnsupportedEncodingException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.support.LdapUtils;

/**
 *
 * @author aimamutdinov
 */
public class LdapAplanaUtils {
	private static final Logger logger = LoggerFactory.getLogger(LdapAplanaUtils.class);
	public static String getSidAttribute(Attribute attribute) throws NamingException {
		if (attribute != null) {
			Object value = attribute.get();
			if (value instanceof byte[]) {
				byte[] result = (byte []) value;
				String resultSid;
				resultSid =decodeSID(result);
				logger.debug("!!!![" + attribute.getID() + "]=" + resultSid);
				return resultSid;
			} else {
				// error
				throw new NamingException("[" + attribute.getID() + "] is not byte[]");
			}
		}
		 throw new NamingException("Attribute is null");
	}
	
	public static String checkAttribute(Attribute attribute) throws NamingException {
		if (attribute != null) {
			Object value = attribute.get();
			if (value instanceof String) {
				return (String) value;
			} else {
				// error
				throw new NamingException("[" + attribute.getID() + "] is not String");
			}
		}
		//throw new NamingException("Attribute is null");
		logger.error("!!!!Attribute is null");
		return null;
	}
	
	/**
     * The binary data is in the form:
     * byte[0] - revision level
     * byte[1] - count of sub-authorities
     * byte[2-7] - 48 bit authority (big-endian)
     * and then count x 32 bit sub authorities (little-endian)
     * 
     * The String value is: S-Revision-Authority-SubAuthority[n]...
     * 
     * Based on code from here - http://forums.oracle.com/forums/thread.jspa?threadID=1155740&tstart=0
     */
    public static String decodeSID(byte[] sid) {
        
        final StringBuilder strSid = new StringBuilder("S-");

        // get version
        final int revision = sid[0];
        strSid.append(Integer.toString(revision));
        
        //next byte is the count of sub-authorities
        final int countSubAuths = sid[1] & 0xFF;
        
        //get the authority
        long authority = 0;
        //String rid = "";
        for(int i = 2; i <= 7; i++) {
           authority |= ((long)sid[i]) << (8 * (5 - (i - 2)));
        }
        strSid.append("-");
        strSid.append(Long.toHexString(authority));
        
        //iterate all the sub-auths
        int offset = 8;
        int size = 4; //4 bytes for each sub auth
        for(int j = 0; j < countSubAuths; j++) {
            long subAuthority = 0;
            for(int k = 0; k < size; k++) {
                subAuthority |= (long)(sid[offset + k] & 0xFF) << (8 * k);
            }
            
            strSid.append("-");
            strSid.append(subAuthority);
            
            offset += size;
        }
        
        return strSid.toString();    
    }
}
