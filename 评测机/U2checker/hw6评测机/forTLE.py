import os
import platform
import subprocess
import multiprocessing
import shutil
from tqdm import tqdm

from checker import check

########## configs you need to modify BEGIN ##########

JAR_NAME = ''
PROCESS_COUNT = os.cpu_count() * 2
ITERATIONS = 1000

########## configs you need to modify END ##########

CACHE_PATH = "cache"
if platform.system() == 'Windows':
    FEED_PROGRAM = 'datainput_student_win64.exe'
else:
    FEED_PROGRAM = './datainput_student_linux_x86_64'


def run_iteration(iteration):
    cache_folder = os.path.join(CACHE_PATH, f"tle_iteration_{iteration}")
    os.makedirs(cache_folder, exist_ok=True)

    stdin_path = os.path.join(os.getcwd(), "stdin.txt")
    shutil.copy(FEED_PROGRAM, cache_folder)
    shutil.copy(stdin_path, cache_folder)

    # run your program
    stdout_path = os.path.join(cache_folder, f"stdout.txt")
    with open(stdout_path, "w") as stdout_file:
        datainput_proc = subprocess.Popen([FEED_PROGRAM], cwd=cache_folder, stdout=subprocess.PIPE,
                                          stderr=subprocess.STDOUT)
        java_proc = subprocess.Popen(["java", "-jar", JAR_NAME], stdin=datainput_proc.stdout,
                                     stdout=stdout_file, stderr=subprocess.STDOUT)

    try:
        return_code = java_proc.wait(timeout=120)
        if return_code is None or return_code != 0:
            java_proc.kill()
            java_proc.wait()
            datainput_proc.kill()
            datainput_proc.wait()
            return f"tle_{iteration}: Error-{return_code}", cache_folder
    except subprocess.TimeoutExpired:
        java_proc.kill()
        java_proc.wait()
        datainput_proc.kill()
        datainput_proc.wait()
        check(input_path=stdin_path, output_path=stdout_path)
        return f"tle_{iteration}: OverTime1", cache_folder

    # 检查子进程是否已经结束
    if java_proc.poll() is None:
        java_proc.kill()
        java_proc.wait()
        datainput_proc.kill()
        datainput_proc.wait()
        return f"tle_{iteration}: OverTime2", cache_folder
    if java_proc.poll() != 0:
        java_proc.kill()
        java_proc.wait()
        datainput_proc.kill()
        datainput_proc.wait()
        return f"tle_{iteration}: OverTime3", cache_folder
    if java_proc.stderr:
        java_proc.kill()
        java_proc.wait()
        datainput_proc.kill()
        datainput_proc.wait()
        return f"tle_{iteration}: Error2", cache_folder

    # 运行 checker，传递 stdin.txt 和 stdout.txt 的路径作为命令行参数
    if not check(input_path=stdin_path, output_path=stdout_path):
        java_proc.kill()
        java_proc.wait()
        datainput_proc.kill()
        datainput_proc.wait()
        return f"tle_{iteration} didn't pass checker", cache_folder
    else:
        java_proc.kill()
        java_proc.wait()
        datainput_proc.kill()
        datainput_proc.wait()
        return "Correct", cache_folder


def run():
    pool = multiprocessing.Pool(processes=PROCESS_COUNT, maxtasksperchild=PROCESS_COUNT)

    iterations = range(1, ITERATIONS + 1)

    with tqdm(total=len(iterations), desc="Iterations") as pbar:
        for result in pool.imap_unordered(run_iteration, iterations):
            pbar.update()
            if result[0] != "Correct":
                print(result[0])
                with open("tle_res.txt", "a+") as f:
                    f.write(result[0] + "\n")
            else:
                shutil.rmtree(result[1])
    pool.close()
    pool.join()


if __name__ == "__main__":
    run()
