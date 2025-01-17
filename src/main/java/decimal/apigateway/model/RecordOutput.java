package decimal.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordOutput implements Serializable {

	private static final long serialVersionUID = 1L;
	private String recordOutputArray;
	private String recordError;
	private String primaryKeyValue;

	public RecordOutput(String primaryKeyValue)
	{
		this.primaryKeyValue = primaryKeyValue;
	}
}
