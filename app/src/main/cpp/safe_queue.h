#ifndef JNISTUDY_SAFE_QUEUE_H
#define JNISTUDY_SAFE_QUEUE_H

#include "../../../../../../Library/Android/sdk/ndk/20.1.5948944/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/c++/v1/queue"
#include "../../../../../../Library/Android/sdk/ndk/20.1.5948944/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/pthread.h"
#include "macro.h"

using namespace std;

template<typename T>
class SafeQueue {
    typedef void (*ReleaseCallback)(T *);

    typedef void (*SyncCallback)(queue<T> &);

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
            q.push(value);
            pthread_cond_signal(&cond);
        } else {
            if (releaseCallback) {
                releaseCallback(&value);
            }
        }
        pthread_mutex_unlock(&mutex);
    }

    void sync() {
        pthread_mutex_lock(&mutex);
        if (syncCallback) {
            syncCallback(this->q);
        }
        pthread_mutex_unlock(&mutex);
    }

    /**
      * 出队
      */
    int pop(T &value) {
        int ret = 0;
        pthread_mutex_lock(&mutex);
        while (working && q.empty()) {
            //如果工作状态，队列中没有数据
            pthread_cond_wait(&cond, &mutex);
        }
        if (!q.empty()) {
            value = q.front();
            q.pop();
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

    bool isWorking() {
        return working;
    }

    void setReleaseCallback(ReleaseCallback callback) {
        this->releaseCallback = callback;
    }

    void setSyncCallback(SyncCallback callback) {
        this->syncCallback = callback;
    }

    int empty() {
        return q.empty();
    }

    int size() {
        return q.size();
    }

    void clear() {
        pthread_mutex_lock(&mutex);
        unsigned int size = q.size();
        for (int i = 0; i < size; ++i) {
            //循环释放队列中的数据
            T value = q.front();
            if(releaseCallback){
                releaseCallback(&value);
            }
            q.pop();
        }
        pthread_mutex_unlock(&mutex);
    }

private:
    queue<T> q;
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    int working;
    ReleaseCallback releaseCallback;
    SyncCallback syncCallback;
};


#endif //JNISTUDY_SAFE_QUEUE_H
