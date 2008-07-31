package templates.model;

public interface TemplateModelPublisher
{
	/**
	 * Attaches the given subscriber to this publisher. The given subscriber
	 * will receive notifications of changes from this publisher.
	 * 
	 * @param subscriber
	 */
	public void addSubscriber(TemplateModelSubscriber subscriber);

	/**
	 * Removes the given subscriber to this publisher. The given subscriber will
	 * no longer receive notifications of changes from this publisher.
	 * 
	 * @param subscriber
	 */
	public void removeSubscriber(TemplateModelSubscriber subscriber);

	/**
	 * Returns all current subscribers to this publisher.
	 * 
	 * @return all current subscribers to this publisher
	 */
	public TemplateModelSubscriber[] getTemplateModelSubscribers();

	/**
	 * Triggers a notification to all subscribers that the structure of the FSA
	 * model has changed.
	 * 
	 * @param message
	 *            message with additional info about the change
	 */
	public void fireTemplateModelStructureChanged(TemplateModelMessage message);
}
