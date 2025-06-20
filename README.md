# code-with-quarkus

this reproducer shows that we can't rely on duplicated context when consuming jms messages and sending them to kafka
the scenario goes like this:
- a timer sends a jms message to a queue with a correlation id
- a jms consumer consumes the message, fetches the correlation id from the jms message, attempts to set in the duplicated context (which will fail) and sends the message to kafka through an emitter
- a kafka outgoing message interceptor attempts to read the correlation id from the duplicated context (which will fail) to set it as a header in the kafka message
- a kafka consumer consumes the message and attempts to read the correlation id from the header (which will be missing) 

we can see 3 error logs:
- `unable to set correlation id in duplicated context` when the jms consumer fails to set the correlation id in the duplicated context
- `Error retrieving correlation ID from context locals` when the kafka interceptor fails to get the correlation id from the duplicated context
- `metadata correlationid: ======= MISSING!! =======` when the kafka consumer fails to read the correlation id from the header

expected behavior: we should be able to access the duplicated context from the thread used to consume the jms message, including running the kafka interceptor.