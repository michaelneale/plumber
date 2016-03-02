package dsl


// The call(body) method in any file in workflowLibs.git/vars is exposed as a
// method with the same name as the file.
def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    /** Do some work */
    echo config.name

}
