import os
import platform
import subprocess
import multiprocessing
import shutil
from random import randint
from tqdm import tqdm
from checker import check  # 保留原有 checker.py

CACHE_PATH = "cache"
if platform.system() == 'Windows':
    FEED_PROGRAM = 'datainput_student_win64.exe'
    SHELL = True
else:
    FEED_PROGRAM = './datainput_student_linux_x86_64'
    SHELL = False


# 对单个 .jar 文件执行逻辑
def process_jar_file(jar_file_path, cache_folder, stdin_path):
    stdout_path = os.path.join(cache_folder, f"stdout_{jar_file_path}.txt")

    with open(stdout_path, "w") as stdout_file:
        datainput_proc = subprocess.Popen([FEED_PROGRAM], cwd=cache_folder, stdout=subprocess.PIPE,
                                          stderr=subprocess.STDOUT)
        java_proc = subprocess.Popen(["java", "-jar", jar_file_path], stdin=datainput_proc.stdout,
                                     stdout=stdout_file, stderr=subprocess.STDOUT, shell=SHELL)

    try:
        return_code = java_proc.wait(timeout=120)
        if return_code is None or return_code != 0:
            return f"Error-{return_code}"
    except subprocess.TimeoutExpired:
        return "OverTime1"

    if java_proc.poll() is None:
        return "OverTime2"
    if java_proc.poll() != 0:
        return "OverTime3"
    if java_proc.stderr:
        return "Error2"

    checker_output = check(stdin_path, stdout_path)
    if checker_output is False:
        return "Error3"
    else: 
        print("\nCorrect.")
        return "Correct"


def get_jar_files(directory):
    return [f for f in os.listdir(directory) if f.endswith('.jar')]


def start_processes(jar_files):
    # 生成一个随机数作为processor_id
    processor_id = randint(0, 100000)
    # 生成一个缓存文件夹路径
    cache_folder = os.path.join(CACHE_PATH, f"{processor_id}_art")
    try:
        # 创建缓存文件夹
        os.makedirs(cache_folder, exist_ok=True)
        # 标记缓存文件夹是否需要删除
        to_be_deleted = True

        # 直接使用当前目录下的 stdin.txt
        # 获取当前目录下的 stdin.txt 文件路径
        src_stdin_path = os.path.join(os.getcwd(), "stdin.txt")
        # 在缓存文件夹下创建一个 stdin.txt 文件
        stdin_path = os.path.join(cache_folder, "stdin.txt")
        # 将当前目录下的 stdin.txt 文件复制到缓存文件夹下
        shutil.copy(src_stdin_path, stdin_path)

        # 将 FEED_PROGRAM 复制到缓存文件夹下
        shutil.copy(FEED_PROGRAM, cache_folder)

        # 使用多进程池
        with multiprocessing.Pool() as pool:
            # 创建任务列表
            tasks = [(jar_file, pool.apply_async(process_jar_file, (jar_file, cache_folder, stdin_path)))
                     for jar_file in jar_files]

            # 遍历任务列表
            for fname, task in tasks:
                try:
                    # 获取任务结果
                    result = task.get()
                    # 如果结果不是 "Correct"，则标记缓存文件夹不需要删除，并打印错误信息
                    if result != "Correct":
                        to_be_deleted = False
                        print(f"Error: {result}")
                        # 将 stdin.txt 文件内容写入到错误文件中
                        with open(f"{result}_by_{fname}_at_{processor_id}.txt", "w") as fout, open(stdin_path, 'r') as fin:
                            fout.write(fin.read())
                except KeyboardInterrupt:
                    # 如果捕获到 KeyboardInterrupt 异常，则退出循环
                    pass

        # 如果缓存文件夹需要删除，则删除缓存文件夹
        if to_be_deleted:
            shutil.rmtree(cache_folder)

    except KeyboardInterrupt:
        # 如果捕获到 KeyboardInterrupt 异常，则退出循环
        pass


def main():
    directory = os.getcwd()
    jar_files = get_jar_files(directory)
    print(jar_files)

    all_performances = {}
    for _ in tqdm(range(1)):
        if not jar_files:
            continue

        performances = start_processes(jar_files)
        if performances is None:
            continue
        for jar_file, performance in performances.items():
            if jar_file not in all_performances:
                all_performances[jar_file] = []
            all_performances[jar_file].append(performance)


if __name__ == "__main__":
    main()
