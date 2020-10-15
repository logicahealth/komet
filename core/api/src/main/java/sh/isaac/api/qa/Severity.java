package sh.isaac.api.qa;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public enum Severity
{
	FATAL, ERROR, WARNING, NOTICE
}
