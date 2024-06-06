// A node of the tree
class Node {
  public:
    int key;
    Node* left;
    Node* right;
    Node* parent;

    // Constructor
    Node(int key, Node* parent=nullptr) {
        this->key = key;
        this->parent = parent;
        this->left = nullptr;
        this->right = nullptr;
    }
};

// Binary tree
class Tree {
  public:
    // Pointer to root of the tree; nullptr if the tree is empty.
    Node* root = nullptr;

    // Insert a key into the tree.
    // If the key is already present, nothing happens.
    void insert(int key) {
        if (!root) {
            root = new Node(key);
            return;
        }

        Node* node = root;
        while (node->key != key) {
            if (key < node->key) {
                if (!node->left)
                    node->left = new Node(key, node);
                node = node->left;
            } else {
                if (!node->right)
                    node->right = new Node(key, node);
                node = node->right;
            }
        }
    }

    // Return successor of the given node.
    //
    // The successor of a node is the node with the next higher key.
    // Return nullptr if there is no such node.
    // If the argument is nullptr, return the node with the smallest key.
    Node* successor(Node* node) {
        // TODO: Implement
    }

    // Destructor to free all allocated memory.
    ~Tree() {
        Node* node = root;
        while (node) {
            Node* next;
            if (node->left) {
                next = node->left;
                node->left = nullptr;
            } else if (node->right) {
                next = node->right;
                node->right = nullptr;
            } else {
                next = node->parent;
                delete node;
            }
            node = next;
        }
    }
};
