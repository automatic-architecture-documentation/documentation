package documentation.generators.plantuml

import documentation.model.Application
import documentation.model.ComponentType
import documentation.model.ComponentType.BACKEND
import documentation.model.Messaging.Binding
import documentation.model.Messaging.ConsumedQueue
import documentation.model.Messaging.PublishedMessage
import documentation.model.componentName

class MessagingDiagramGenerator(
    private val applications: List<Application>,
    private val options: Options = Options(),
) : AbstractDiagramGenerator(options) {

    data class Options(
        override val lineType: LineType = LineType.DEFAULT,
        override val includedComponentTypes: Set<ComponentType> = setOf(BACKEND),
        override val includeCredentials: Boolean = false,
    ) : DiagramGeneratorOptions

    private val publishingComponents: List<DiagramComponent>
    private val publishingNotes: List<DiagramNote>
    private val exchanges: List<DiagramComponent>
    private val queues: List<DiagramComponent>
    private val consumingComponents: List<DiagramComponent>
    private val relationships: List<DiagramRelationship>

    init {
        publishingComponents = publishingApplications()
            .map { diagramComponent(it, "producer") }
        publishingNotes = publishingApplications()
            .map(::publishingNote)
        exchanges = getAllExchangeNames()
            .map(::diagramExchangeComponent)
        queues = getAllQueueNames()
            .map(::diagramQueueComponent)
        consumingComponents = consumingApplications()
            .map { diagramComponent(it, "consumer") }

        relationships = buildList {
            publishingApplications()
                .forEach { application ->
                    application.messaging.publishedMessages
                        .map { (exchange) ->
                            publisherToExchangeRelationship(application, exchange)
                        }
                        .forEach(::add)
                }
            consumingApplications()
                .forEach { application ->
                    application.messaging.consumedQueues
                        .forEach { (name, bindings) ->
                            bindings.forEach { (exchange, routingKeyPattern) ->
                                add(exchangeToQueueRelationship(exchange, name, routingKeyPattern))
                            }
                            add(queueToConsumerRelationship(name, application))
                        }
                }
        }
    }

    override fun plantUmlSource(): String =
        buildString {
            appendLine("@startuml")
            appendLine("'https://plantuml.com/deployment-diagram")
            appendLine()
            appendLine("scale 2")
            appendLine()
            appendLine("left to right direction")
            appendLine()
            appendLine(options.lineType)
            appendLine()
            publishingComponents.forEach { appendComponentLine(it) }
            appendLine("""frame "Message Broker" {""")
            exchanges.forEach { appendComponentLine(it) }
            queues.forEach { appendComponentLine(it) }
            appendLine("}")
            consumingComponents.forEach { appendComponentLine(it) }
            appendLine()
            publishingNotes.forEach { appendNote(it) }
            appendLine()
            relationships.forEach { appendRelationshipLine(it) }
            appendLine()
            appendLine("@enduml")
        }

    private fun diagramExchangeComponent(it: String) =
        DiagramComponent(id = diagramExchangeId(it), type = "boundary", name = it, style = null)

    private fun diagramQueueComponent(it: String) =
        DiagramComponent(id = diagramQueueId(it), type = "queue", name = it, style = null)

    private fun publisherToExchangeRelationship(publisher: Application, exchange: String) =
        DiagramRelationship(
            source = diagramComponentId(publisher, "producer"),
            target = diagramExchangeId(exchange),
            link = "-->",
        )

    private fun exchangeToQueueRelationship(exchange: String, queue: String, routingKeyPattern: String) =
        DiagramRelationship(
            source = diagramExchangeId(exchange),
            target = diagramQueueId(queue),
            link = "-->",
            label = routingKeyPattern
        )

    private fun queueToConsumerRelationship(queue: String, consumer: Application) =
        DiagramRelationship(
            source = diagramQueueId(queue),
            target = diagramComponentId(consumer, "consumer"),
            link = "-->",
        )

    private fun publishingNote(application: Application) = DiagramNote(
        target = diagramComponentId(application, "producer"),
        text = publishingNoteText(application).trim(),
        position = "left"
    )

    private fun publishingNoteText(application: Application) =
        buildString {
            application.messaging.publishedMessages
                .forEach { (exchange, routingKeys) ->
                    appendLine("**$exchange**:")
                    routingKeys.forEach { routingKey ->
                        appendLine(" - $routingKey")
                    }
                    appendLine()
                }
        }

    private fun getAllExchangeNames(): List<String> =
        (getPublishingExchangeNames() + getConsumingExchangeNames()).distinct()

    private fun getPublishingExchangeNames(): List<String> =
        applications
            .flatMap { application -> application.messaging.publishedMessages.map(PublishedMessage::exchange) }
            .distinct()

    private fun getConsumingExchangeNames(): List<String> =
        applications
            .flatMap { application -> application.messaging.consumedQueues.flatMap { it.bindings.map(Binding::exchange) } }
            .distinct()

    private fun getAllQueueNames(): List<String> =
        applications
            .flatMap { application -> application.messaging.consumedQueues.map(ConsumedQueue::name) }
            .distinct()

    private fun publishingApplications() = applications
        .filter { it.messaging.publishedMessages.isNotEmpty() }
        .sortedBy { componentName(it.id) }

    private fun consumingApplications() = applications
        .filter { it.messaging.consumedQueues.isNotEmpty() }
        .sortedBy { componentName(it.id) }

    private fun diagramExchangeId(it: String) = "exchange__${normalizeIdPart(it)}"
    private fun diagramQueueId(it: String) = "queue__${normalizeIdPart(it)}"
}
