package util.graph

class Node<T>(val item: T, val edges: MutableList<Node<T>> = mutableListOf()) {
}
