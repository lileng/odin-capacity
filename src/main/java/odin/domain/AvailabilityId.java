package odin.domain;

import java.io.Serializable;

public class AvailabilityId implements Serializable {
 
    private static final long serialVersionUID = 1L;
 
    private Long individual;
    private Long week;

 
    @Override
    public int hashCode() {
        return (int)(individual + week);
    }
 
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AvailabilityId){
        	AvailabilityId availabilityId = (AvailabilityId) obj;
            return availabilityId.individual == individual && availabilityId.week == week;
        }
 
        return false;
    }
}