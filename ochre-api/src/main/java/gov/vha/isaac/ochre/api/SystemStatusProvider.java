package gov.vha.isaac.ochre.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.util.Pair;
import gov.vha.isaac.ochre.api.SystemStatusService;
import org.jvnet.hk2.annotations.Service;

@Service
public class SystemStatusProvider implements SystemStatusService
{
	private List<Pair<String, Exception>> configurationFailures = null;

	private SystemStatusProvider()
	{
		// for HK2
	}

	/**
	 * @see gov.vha.isaac.ochre.api.SystemStatusService#notifyServiceConfigurationFailure(java.lang.String, java.lang.Exception)
	 */
	@Override
	public void notifyServiceConfigurationFailure(String serviceName, Exception failureDetails)
	{
		if (configurationFailures == null)
		{
			configurationFailures = new ArrayList<>();
		}
		configurationFailures.add(new Pair<String, Exception>(serviceName, failureDetails));
	}

	/**
	 * 
	 * @see gov.vha.isaac.ochre.api.SystemStatusService#getServiceConfigurationFailures()
	 */
	@Override
	public Optional<List<Pair<String, Exception>>> getServiceConfigurationFailures()
	{
		return Optional.of(configurationFailures);
	}
}
