package templates.presentation;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;

import templates.diagram.DiagramElement;

public class IssueDescriptor
{
	public static final int TYPE_ERROR = 1;

	public static final int TYPE_WARNING = 2;

	public String message;

	public int type;

	public List<Action> fixes;

	public Collection<DiagramElement> elements;

	public IssueDescriptor(String message, int type,
			Collection<DiagramElement> elements, List<Action> fixes)
	{
		this.message = message;
		this.elements = new HashSet<DiagramElement>(elements);
		this.type = type;
		this.fixes = new LinkedList<Action>(fixes);
	}
}
