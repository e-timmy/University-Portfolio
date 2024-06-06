#include <algorithm>
#include <functional>
#include <cstdint>
#include <string>
#include <utility>
#include <vector>

#include "tree_successor.h"

using namespace std;

// If the condition is not true, report an error and halt.
#define EXPECT(condition, message) do { if (!(condition)) expect_failed(message); } while (0)
void expect_failed(const string& message);

void test(const vector<int>& sequence) {
    Tree tree;
    for (const auto& element : sequence)
        tree.insert(element);

    vector<int> sorted_sequence(sequence);
    sort(sorted_sequence.begin(), sorted_sequence.end());

    Node* node = tree.successor(nullptr);
    for (const auto& element : sorted_sequence) {
        EXPECT(node, "Expected successor " + to_string(element) + ", got nullptr");
        EXPECT(node->key == element,
               "Expected successor " + to_string(element) + ", got " + to_string(node->key));
        node = tree.successor(node);
    }
    EXPECT(!node, "Expected no successor, got " + to_string(node->key));
}

vector<pair<string, function<void()>>> tests = {
    {"right_path", []
        { vector<int> numbers;
            for (int i = 0; i < 10000; i++) numbers.push_back((int)(7.13*i));
            test(numbers);
        }
    },
    {"left_path", []
        { vector<int> numbers;
            for (int i = 9999; i >= 0; i--) numbers.push_back((int)(7.13*i));
            test(numbers);
        }
    },
    {"random_tree", []
        { vector<int> numbers = {997};
            for (int i = 2; i < 199999; i++)
                numbers.push_back((numbers.back() * int64_t(997)) % 199999);
            test(numbers);
        }
    },
};
