#include <algorithm>
#include <cassert>
#include <fstream>
#include <functional>
#include <string>
#include <utility>
#include <vector>

#include "splay_operation.h"

using namespace std;

// If the condition is not true, report an error and halt.
#define EXPECT(condition, message) do { if (!(condition)) expect_failed(message); } while (0)
void expect_failed(const string& message);

// Flatten the tree: return a sorted list of all keys in the tree.
vector<int> flatten(const Tree& tree) {
    constexpr int L = 0, R = 1, F = 2;

    Node* node = tree.root;
    vector<int> flattened, stack = {L};
    while (!stack.empty()) {
        if (stack.back() == L) {
            stack.back() = R;
            if (node->left) {
                node = node->left;
                stack.push_back(L);
            }
        } else if (stack.back() == R) {
            flattened.push_back(node->key);
            stack.back() = F;
            if (node->right) {
                node = node->right;
                stack.push_back(L);
            }
        } else {
            node = node->parent;
            stack.pop_back();
        }
    }
    return flattened;
}

// Test for splay operation with required helpers
class TestSplay {
  public:
    static Node* deserialize_node(const string& text, int& index) {
        EXPECT(text[index++] == '(', "Internal error during example deserialization");
        if (text[index] == ')') {
            index++;
            return nullptr;
        } else {
            int comma = text.find(',', index);
            int key = stoi(text.substr(index, comma - index));
            Node* left = deserialize_node(text, (index=comma + 1));
            Node* right = deserialize_node(text, ++index);
            EXPECT(text[index++] == ')', "Internal error during example deserialization");
            return new Node(key, nullptr, left, right);
        }
    }

    static Node* deserialize_root(const string& text) {
        int index = 0;
        Node* root = deserialize_node(text, index);
        assert(index == text.size());
        return root;
    }

    static string compare(Node* system, Node* gold) {
        if (!system && gold) {
            return "expected node with key " + to_string(gold->key) + ", found None";
        } else if (system && !gold) {
            return "expected None, found node with key " + to_string(system->key);
        } else if (system && gold) {
            if (system->key != gold->key)
                return "expected node with key " + to_string(gold->key) + ", found " + to_string(system->key);
            auto result = compare(system->left, gold->left);
            if (!result.empty()) return result;
            return compare(system->right, gold->right);
        }
        return string();
    }

    static void test() {
        ifstream splay_tests_file("splay_tests.txt");
        EXPECT(splay_tests_file.is_open(), "Cannot open splay_tests.txt file with the tests");

        string original, splayed;
        int target;
        while (splay_tests_file >> original >> target >> splayed) {
            Tree original_tree(deserialize_root(original));
            Tree splayed_tree(deserialize_root(splayed));

            Node* target_node = original_tree.root;
            while (target_node && target_node->key != target)
                if (target < target_node->key)
                    target_node = target_node->left;
                else
                    target_node = target_node->right;
            EXPECT(target_node, "Internal error during finding the target node in the tree to splay");

            original_tree.splay(target_node);
            auto error = compare(original_tree.root, splayed_tree.root);
            EXPECT(error.empty(), "Error running splay on key " + to_string(target) + " of " + original + ": " + error);
        }
    }
};

void test_lookup() {
    // Insert even numbers
    Tree tree;
    for (int i = 0; i < 5000000; i += 2)
        tree.insert(i);

    // Find non-existing
    for (int i = 1; i < 5000000; i += 2)
        for (int j = 0; j < 10; j++)
            EXPECT(!tree.lookup(i), "Non-existing element was found");

    // Find existing
    for (int i = 0; i < 5000000; i += 2)
        for (int j = 0; j < 10; j++)
            EXPECT(tree.lookup(i), "Existing element was not found");
}

void test_insert() {
    // Test validity first
    {
        Tree tree;
        vector<int> sequence = {997};
        for (int i = 2; i < 1999; i++)
            sequence.push_back((sequence.back() * sequence.front()) % 1999);
        for (const auto& i : sequence)
            tree.insert(i);

        vector<int> flattened = flatten(tree);
        sort(sequence.begin(), sequence.end());
        EXPECT(flattened == sequence, "Incorrect tree after a sequence of inserts");
    }

    // Test speed
    {
        Tree tree;
        for (int i = 0; i < 5000000; i++)
            for (int j = 0; j < 10; j++)
                tree.insert(i);
    }
}

void test_remove() {
    // Test validity first
    {
        Tree tree;
        for (int i = 2; i < 1999 * 2; i++)
            tree.insert(i);

        vector<int> sequence = {2 * 997};
        for (int i = 2; i < 1999; i++)
            sequence.push_back(2 * ((sequence.back() * sequence.front() / 4) % 1999));
        for (const auto& i : sequence)
            tree.remove(i + 1);

        vector<int> flattened = flatten(tree);
        sort(sequence.begin(), sequence.end());
        EXPECT(flattened == sequence, "Correct tree after a sequence of removes");
    }

    // Test speed
    {
        Tree tree;
        for (int i = 0; i < 5000000; i++)
            tree.insert(i);

        // Non-existing elements
        for (int i = 1; i < 5000000; i += 2)
            for (int j = 0; j < 10; j++)
                tree.remove(i);

        // Existing elements
        for (int i = 2; i < 5000000; i += 2)
            for (int j = 0; j < 10; j++)
                tree.remove(i);
    }
}

vector<pair<string, function<void()>>> tests = {
    { "splay", TestSplay::test },
    { "lookup", test_lookup },
    { "insert", test_insert },
    { "remove", test_remove },
};
