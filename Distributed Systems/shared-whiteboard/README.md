# Shared Whiteboard
### Shared Whiteboard Application | Distributed Systems Assignment 2
## Date: Spring 2022
## Author: Timothy Holland 

# Description:
- In this project, we developed a whiteboard that can be shared between different peers. For example, I could start a whiteboard on my computer, then others could join (given group name and password). Then, we are all able to draw, make shapes on the canvas, and talk in a chatbox. Each board is managed by an admin, who also has the power to kick any other player off. Functionality to save, and load images drawn on the board is also included.
- We implemented a centralised Server-Client system architecture and justified on the basis of requiring a single whiteboard only. Whilst our system can handle multiple whiteboards it lacks scalability. To expand this application, we would propose introducing server-to-server communication and load-balancing proxies. 
- Our communication paradigm consists of both synchronous and asynchronous protocols. The first handles all validation messages, whereby access must be checked on the server’s end, thus requiring both a request and reply. The latter handles the rest, including all update messages like canvas drawings or chat messages, which require no validation and can merely be propagated throughout the system. The constitutive features of our Message type (behind all peer interactions), as defined by outcomes in the former, and updates in the latter. 
- UML visualisations, the details of the main modules, APIs utilised, and further details can be seen within the report pdf.