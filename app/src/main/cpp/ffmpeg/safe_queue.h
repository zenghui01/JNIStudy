#ifndef JNISTUDY_SAFE_QUEUE_H
#define JNISTUDY_SAFE_QUEUE_H

#include <queue>
#include <pthread.h>
#include "../macro.h"

using namespace std;

template<typename T>
class SafeQueue {
    typedef void (*ReleaseCallback)(T *);

public:
    SafeQueue() {
        pthread_mutex_init(&mutex, 0);
        pthread_cond_init(&cond, 0);
    }

    virtual ~SafeQueue() {
        pthread_mutex_destroy(&mutex);
        pthread_cond_destroy(&cond);
    }

    /**
     * 入队
     * @param value
     */
    void push(T value) {
        pthread_mutex_lock(&mutex);
        if (working) {
            queue.push(value);
            pthread_cond_signal(&cond);
        } else {
            if (releaseCallback) {
                releaseCallback(&value);
            }
        }
        pthread_mutex_unlock(&mutex);
    }

    /**
      * 出队
      */
    int pop(T &value) {
        int ret = 0;
        pthread_mutex_lock(&mutex);
        while (working && queue.empty()) {
            //如果工作状态，队列中没有数据
            pthread_cond_wait(&cond, &mutex);
        }
        if (!queue.empty()) {
            value = queue.front();
            queue.pop();
            ret = 1;
        }
        pthread_mutex_unlock(&mutex);
        return ret;
    }

    void setWorking(int work) {
        pthread_mutex_lock(&mutex);
        this->working = work;
        pthread_cond_signal(&cond);
        pthread_mutex_unlock(&mutex);
    }

    void setReleaseCallback(ReleaseCallback callback) {
        this->releaseCallback = callback;
    }

    int empty() {
        return queue.empty();
    }

    int size() {
        return queue.size();
    }

    void clear() {
        pthread_mutex_lock(&mutex);
        for (int i = 0; i < queue.size(); ++i) {
            T value = queue.front();
            if (releaseCallback) {
                releaseCallback(&value);
            }
            queue.pop();
        }
        pthread_mutex_unlock(&mutex);
    }

private:
    queue<T> queue;
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    int working;
    ReleaseCallback releaseCallback;
};


#endif //JNISTUDY_SAFE_QUEUE_H
