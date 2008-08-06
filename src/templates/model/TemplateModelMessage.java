package templates.model;

public class TemplateModelMessage
{
	public static final int OP_ADD = 1;

	public static final int OP_REMOVE = 2;

	public static final int OP_MODIFY = 4;

	public static final int ELEMENT_COMPONENT = 1;

	public static final int ELEMENT_LINK = 2;

	protected TemplateModel source;

	protected long elementId;

	protected int elementType;

	protected int operationType;

	protected String message;

	public TemplateModelMessage(TemplateModel source, long elementId,
			int elementType, int operationType, String message)
	{
		this.source = source;
		this.elementId = elementId;
		this.elementType = elementType;
		this.operationType = operationType;
		this.message = message;
	}

	public TemplateModelMessage(TemplateModel source, long elementId,
			int elementType, int operationType)
	{
		this(source, elementId, elementType, operationType, null);
	}

	public TemplateModel getSource()
	{
		return source;
	}

	public long getElementId()
	{
		return elementId;
	}

	public int getElementType()
	{
		return elementType;
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
