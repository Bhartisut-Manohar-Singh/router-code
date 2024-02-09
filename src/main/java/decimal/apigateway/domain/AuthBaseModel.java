package decimal.apigateway.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthBaseModel implements Serializable
{

	private static final long serialVersionUID = -5836476157751179100L;
	private String status;

	private String createdOn;
	
	private String createdBy;
	
	private String modifiedOn;
	
	private String modifiedBy;

	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}
}
