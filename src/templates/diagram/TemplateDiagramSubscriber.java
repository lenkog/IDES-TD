package templates.diagram;

public interface TemplateDiagramSubscriber
{
	public void templateDiagramChanged(TemplateDiagramMessage message);

	public void templateDiagramSelectionChanged(TemplateDiagramMessage message);
}
