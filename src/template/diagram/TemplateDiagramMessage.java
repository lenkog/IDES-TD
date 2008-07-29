package template.diagram;

import java.util.Collection;

public class TemplateDiagramMessage
{
	public static final int OP_ADD = 1;

	public static final int OP_REMOVE = 2;

	public static final int OP_MODIFY = 4;

	protected TemplateDiagram source;

	protected Collection<DiagramElement> elements;

	protected int operationType;

	protected String message;

	public TemplateDiagramMessage(TemplateDiagram source,
			Collection<DiagramElement> elements, int operationType)
	{
		this(source, elements, operationType, "");
	}

	public TemplateDiagramMessage(TemplateDiagram source,
			Collection<DiagramElement> elements, int operationType,
			String message)
	{
		this.source = source;
		this.elements = elements;
		this.operationType = operationType;
		this.message = message;
	}

	public TemplateDiagram getSource()
	{
		return source;
	}

	public Collection<DiagramElement> getElements()
	{
		return elements;
	}

	public int getOperationType()
	{
		return operationType;
	}

	public String getMessage()
	{
		return message;
	}
}
